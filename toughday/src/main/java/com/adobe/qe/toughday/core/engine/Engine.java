package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.Main;
import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.core.engine.publishmodes.PublishMode;
import com.adobe.qe.toughday.core.engine.runmodes.Dry;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Engine for running a test suite.
 */
public class Engine {
    protected static final Logger LOG = LogManager.getLogger(Engine.class);
    public static final int RESULT_AGGREATION_DELAY = 1000; //in 1 Second
    protected static final int WAIT_TERMINATION_FACTOR = 30;
    protected static final double TIMEOUT_CHECK_FACTOR = 0.03;
    protected static Random _rnd = new Random();

    private final Configuration configuration;
    private Configuration.GlobalArgs globalArgs;
    private ExecutorService engineExecutorService = Executors.newFixedThreadPool(2);
    private Map<AbstractTest, AtomicLong> counts = new HashMap<>();
    private final ReentrantReadWriteLock engineSync = new ReentrantReadWriteLock();
    private PublishMode publishMode;
    private RunMode runMode;

    /**
     * Constructor
     * @param configuration A Configuration object.
     * @throws InvocationTargetException caused by reflection
     * @throws NoSuchMethodException caused by reflection
     * @throws InstantiationException caused by reflection
     * @throws IllegalAccessException caused by reflection
     */
    public Engine(Configuration configuration)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.configuration = configuration;
        this.globalArgs = configuration.getGlobalArgs();

        Class<? extends PublishMode> publishModeClass =
                ReflectionsContainer.getInstance()
                    .getPublishModeClasses()
                    .get(globalArgs.getPublishMode());

        if(publishModeClass == null) {
            throw new IllegalStateException("A publish mode with the identifier \"" + globalArgs.getPublishMode() + "\" does not exist");
        }
        publishMode = publishModeClass
                .getConstructor(Engine.class)
                .newInstance(this);

        Class<? extends RunMode> runModeClass =
                ReflectionsContainer.getInstance()
                        .getRunModeClasses()
                        .get(globalArgs.getRunMode());

        if(runModeClass == null) {
            throw  new IllegalStateException("A run mode with the identifier \"" + globalArgs.getRunMode() + "\" does not exist");
        }
        runMode = runModeClass.newInstance();

        for(AbstractTest test : configuration.getTestSuite().getTests()) {
            add(test);
        }
    }

    /**
     * Returns the global args
     * @return
     */

    public RunMap getGlobalRunMap() { return publishMode.getGlobalRunMap(); }

    public Configuration getConfiguration() { return configuration; }

    public Configuration.GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    public Map<AbstractTest, AtomicLong> getCounts() {
        return counts;
    }

    public PublishMode getPublishMode() {
        return publishMode;
    }

    /**
     * Recursive method for preparing a test to run.
     * @param test
     * @return this object. (builder pattern)
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    private Engine add(AbstractTest test)
            throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        createRunners(test);
        addToRunMap(test);
        return this;
    }

    private Engine addToRunMap(AbstractTest test) {
        publishMode.getGlobalRunMap().addTest(test);
        counts.put(test, new AtomicLong(0));
        if(test.includeChildren()) {
            for (AbstractTest child : test.getChildren()) {
                addToRunMap(child);
            }
        }
        return this;
    }

    private Engine createRunners(AbstractTest test)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RunnersContainer.getInstance().addRunner(test);
        for(AbstractTest child : test.getChildren()) {
            createRunners(child);
        }
        return this;
    }

    private static class LogStream extends OutputStream {
        Logger logger;
        String mem = "";

        public LogStream(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void write (int b) {
            byte[] bytes = new byte[1];
            bytes[0] = (byte) (b & 0xff);
            mem = mem + new String(bytes);

            if (mem.endsWith ("\n")) {
                mem = mem.substring(0, mem.length () - 1);
                flush ();
            }
        }

        /**
         * Flushes the output stream.
         */
        public void flush () {
            logger.info(mem);
            mem = "";
        }
    }

    /**
     * Method for starting running tests.
     * @throws Exception
     */
    public void runTests() {
        try {
            run();
        } catch (Exception e) {
            LOG.error("Failure in tests execution ", e);
        }
    }

    public static void printConfiguration(Configuration configuration, PrintStream out) throws InvocationTargetException, IllegalAccessException {
        out.println("#################### Configuration ######################");
        out.println("Global configuration:");
        printObject(configuration.getTestSuite(), out, configuration.getGlobalArgs());

        out.println("Tests:");
        for(AbstractTest test : configuration.getTestSuite().getTests()) {
            printObject(configuration.getTestSuite(), out, test);
        }

        out.println("Publishers:");
        for(Publisher publisher : configuration.getGlobalArgs().getPublishers()) {
            printObject(configuration.getTestSuite(), out, publisher);
        }
        out.println("#########################################################");
    }

    public static void installToughdayContentPackage(Configuration.GlobalArgs globalArgs) throws Exception {
        logGlobal("Installing ToughDay 2 Content Package...");
        PackageManagerClient packageManagerClient = SequentialTestBase.createClient(globalArgs).adaptTo(PackageManagerClient.class);

        String tdContentPackageGroup = "com.adobe.qe.toughday";
        String tdContentPackageName = ReflectionsContainer.getInstance().getToughdayContentPackage();

        //Upload and install test content package
        if(packageManagerClient.isPackageCreated(tdContentPackageName, tdContentPackageGroup)) {
            packageManagerClient.deletePackage(tdContentPackageName, tdContentPackageGroup);
        }

        packageManagerClient.uploadPackage(
                Engine.class.getClassLoader().getResourceAsStream(tdContentPackageName), tdContentPackageName);
        packageManagerClient.installPackage(tdContentPackageName, tdContentPackageGroup);
        logGlobal("Finished installing ToughDay 2 Content Package.");
    }

    public static void printObject(TestSuite testSuite, PrintStream out, Object obj)
            throws InvocationTargetException, IllegalAccessException {
        Class objectClass = obj.getClass();
        out.println("- Configuration for object of class " + objectClass.getSimpleName());
        out.println(String.format("\t%-32s %-64s", "Property", "Value"));
        for(Method method : objectClass.getMethods()) {
            if (method.isAnnotationPresent(ConfigArgGet.class)) {
                ConfigArgGet configArg = method.getAnnotation(ConfigArgGet.class);

                printObjectProperty(out,
                        StringUtils.isEmpty(configArg.name()) ? Configuration.propertyFromMethod(method.getName()) : configArg.name(),
                        method.invoke(obj));
            }
        }
        if(AbstractTest.class.isAssignableFrom(objectClass)) {
            AbstractTest test = (AbstractTest) obj;

            Long count = testSuite.getCount(test);
            Long timeout = testSuite.getTimeout(test);
            Integer weight = testSuite.getWeightMap().get(test);
            printObjectProperty(out, "weight",  weight != null ? weight : 1);
            printObjectProperty(out, "timeout", timeout != null ? timeout : Configuration.GlobalArgs.DEFAULT_TIMEOUT);
            printObjectProperty(out, "count", count != null ? count : "none");
        }
        out.println();
        out.println();
    }

    public static void printObjectProperty(PrintStream out, String propertyName, Object propertyValue) {
        out.println(String.format("\t%-32s %-64s", propertyName, propertyValue));
    }

    public static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z")
                .format(Calendar.getInstance().getTime());
    }

    public static void runFactorySetup(AbstractTest test) throws Exception {
        for (AbstractTest child : test.getChildren()) {
            runFactorySetup(child);
        }
        for (Method method : test.getClass().getDeclaredMethods()) {
            if (method.getAnnotation(FactorySetup.class) != null) {
                method.setAccessible(true);
                method.invoke(test);
            }
        }
    }

    private void run() throws Exception {
        if(globalArgs.getInstallSampleContent() && !runMode.isDryRun()) {
            printConfiguration(configuration, new PrintStream(new LogStream(LOG)));
            installToughdayContentPackage(globalArgs);
        }

        Engine.logGlobal(String.format("Running tests for %s seconds or until count for all tests has been reached",
                configuration.getGlobalArgs().getDuration()));

        Engine.logGlobal("Test execution started at: " + Engine.getCurrentDateTime());
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                Engine.logGlobal("Test execution finished at: " + Engine.getCurrentDateTime());
            }
        });
        TestSuite testSuite = configuration.getTestSuite();

        // Run the setup step of the suite
        if (testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }

        //TODO move this to a better place
        for(AbstractTest test : testSuite.getTests()) {
            runFactorySetup(test);
        }

        publishMode.getGlobalRunMap().reinitStartTimes();

        RunMode.RunContext context = runMode.runTests(this);
        if (runMode.isDryRun())
            return;

        // Create the result aggregator thread
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(this, context);
        engineExecutorService.execute(resultAggregator);

        // create the timeout checker thread
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(this, configuration.getTestSuite(), context, Thread.currentThread());
        engineExecutorService.execute(timeoutChecker);
        // This thread sleeps until the duration
        try {
            Thread.sleep(globalArgs.getDuration() * 1000L);
        } catch (InterruptedException e) {
            LOG.info("Engine Interrupted", e);
        }
        // Then close all threads
        finally {
            runMode.finishExecution();
            resultAggregator.finishExecution();
            timeoutChecker.finishExecution();

            // interrupt extra test threads
            // TODO: this is suboptimal, replace with a better mechanism for notifications
            List<Thread> threadsList = AbstractTest.getExtraThreads();
            synchronized (threadsList) {
                for (Thread t : threadsList) {
                    t.interrupt();
                }
            }

            shutdownAndAwaitTermination(runMode.getExecutorService());
            shutdownAndAwaitTermination(engineExecutorService);
            publishMode.publishFinalResults();

            LOG.info("Test execution finished at: " + getCurrentDateTime());
        }

    }


    public ReentrantReadWriteLock getEngineSync() {
        return engineSync;
    }

    /**
     * Method for getting the next weighted random test form the test suite
     * TODO: optimize
     */
    public static AbstractTest getNextTest(TestSuite testSuite, Map<AbstractTest, AtomicLong> counts, ReentrantReadWriteLock engineSync) throws InterruptedException {
        //If we didn't find the next test we start looking for it assuming that not all counts are done
        while (testSuite.getTests().size() != 0) {
            engineSync.readLock().lock();
            try {
                int randomNumber = _rnd.nextInt(testSuite.getTotalWeight());
                for (AbstractTest test : testSuite.getTests()) {
                    int testWeight = testSuite.getWeightMap().get(test);

                    long testRuns = counts.get(test).get();
                    Long maxRuns   = testSuite.getCount(test);

                    //If max runs was exceeded for a test
                    if (null != maxRuns && testRuns > maxRuns) {
                        //Try to acquire the lock for removing the test from the suite
                        engineSync.readLock().unlock();
                        engineSync.writeLock().lock();
                        try {
                            if(!testSuite.contains(test.getName())) { break; }
                            //Remove test from suite
                            testSuite.remove(test);
                            //Start looking for the test from the beginning as the total weight changed
                            break;
                        } finally {
                            engineSync.writeLock().unlock();
                            engineSync.readLock().lock();
                        }
                    }
                    if (randomNumber < testWeight) {
                        return test;
                    }
                    randomNumber = randomNumber - testWeight;
                }
            } finally {
                engineSync.readLock().unlock();
            }
        }
        return null;
    }

    public static void logGlobal(String message) {
        LOG.info(message);
        LogManager.getLogger(Main.class).info(message);
    }

    /**
     * Method for forcing an ExecutorService to finish.
     * @param pool
     */
    protected void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(WAIT_TERMINATION_FACTOR * RESULT_AGGREATION_DELAY, TimeUnit.MILLISECONDS))
                    LOG.error("Thread pool did not terminate. Process must be killed");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
