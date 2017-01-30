package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.Main;
import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.annotations.FactorySetup;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.tests.sequential.SequentialTestBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Engine for running a test suite.
 */
public class Engine {
    protected static final Logger LOG = LogManager.getLogger(Engine.class);
    protected static final int RESULT_AGGREATION_DELAY = 1000; //in ms
    protected static final int WAIT_TERMINATION_FACTOR = 30;
    protected static final double TIMEOUT_CHECK_FACTOR = 0.03;
    protected static Random _rnd = new Random();

    private final Configuration configuration;
    private TestSuite testSuite;
    private Configuration.GlobalArgs globalArgs;
    private ExecutorService testsExecutorService;
    private ExecutorService engineExecutorService;
    private RunMap globalRunMap;
    private final EngineSync engineSync = new EngineSync();

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
        this.testSuite = configuration.getTestSuite();
        this.globalArgs = configuration.getGlobalArgs();
        this.testsExecutorService = Executors.newFixedThreadPool(globalArgs.getConcurrency());
        this.engineExecutorService = Executors.newFixedThreadPool(2);

        this.globalRunMap = new RunMap(globalArgs.getConcurrency());
        for(AbstractTest test : testSuite.getTests()) {
            add(test);
        }
    }

    /**
     * Returns the Global Run map object
     * @return
     */
    protected RunMap getGlobalRunMap() {
        return globalRunMap;
    }

    /**
     * Returns the global args
     * @return
     */
    protected Configuration.GlobalArgs getGlobalArgs() {
        return globalArgs;
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
        globalRunMap.addTest(test);
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
            switch (globalArgs.getRunModeEnum()) {
                case DRY:
                    System.out.println("NOTE: This is just a dry run. No test is actually executed.");
                    printConfiguration(configuration, System.out);
                    break;
                case NORMAL:
                    printConfiguration(configuration, new PrintStream(new LogStream(LOG)));
                    //TODO? move this someplace else?
                    if(globalArgs.getInstallSampleContent()) {
                        installToughdayContentPackage(globalArgs);
                    }
                    run();
                    break;
            }
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

    private static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z")
                .format(Calendar.getInstance().getTime());
    }

    private void runFactorySetup(AbstractTest test) throws Exception {
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
        logGlobal(String.format("Running tests for %s seconds or until count for all tests has been reached",
                configuration.getGlobalArgs().getDuration()));

        logGlobal("Test execution started at: " + getCurrentDateTime());
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logGlobal("Test execution finished at: " + getCurrentDateTime());
            }
        });


        // Run the setup step of the suite
        if (testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }

        //TODO move this to a better place
        for(AbstractTest test : testSuite.getTests()) {
            runFactorySetup(test);
        }

        globalRunMap.reinitStartTimes();

        // Create the test worker threads
        List<AsyncTestWorker> testWorkers = new ArrayList<>();
        for (int i = 0; i < globalArgs.getConcurrency(); i++) {
            AsyncTestWorker runner = new AsyncTestWorker(this, testSuite, globalRunMap.newInstance());
            testWorkers.add(runner);
            testsExecutorService.execute(runner);
        }

        // Create the result aggregator thread
        AsyncResultAggregator resultAggregator = new AsyncResultAggregator(this, testWorkers);
        engineExecutorService.execute(resultAggregator);

        // create the timeout chekcer thread
        AsyncTimeoutChecker timeoutChecker = new AsyncTimeoutChecker(this, testSuite, testWorkers, Thread.currentThread());
        engineExecutorService.execute(timeoutChecker);

        // This thread sleeps until the duration
        try {
            Thread.sleep(globalArgs.getDuration() * 1000L);
        } catch (InterruptedException e) {
            LOG.info("Engine Interrupted", e);
        }
        // Then close all threads
        finally {
            for (AsyncTestWorker run : testWorkers) {
                run.finishExecution();
            }
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

            shutdownAndAwaitTermination(testsExecutorService);
            shutdownAndAwaitTermination(engineExecutorService);
            publishFinalResults();

            LOG.info("Test execution finished at: " + getCurrentDateTime());
        }
    }

    /**
     * Publish final results.
     */
    private void publishFinalResults() {
        for (Publisher publisher : globalArgs.getPublishers()) {
            publisher.publishFinal(globalRunMap.getTestStatistics());
        }
    }

    private static class EngineSync {
        private final Lock lock = new ReentrantLock();
        private final AtomicInteger readThreads = new AtomicInteger(0);
        private final ManualResetEvent event = new ManualResetEvent();

        public void subscribe() throws InterruptedException {
            synchronized (event.getMonitor()) {
                event.waitOne();
                readThreads.incrementAndGet();
            }
        }

        public void holdSubscribeAndAwaitExisting() {
            event.reset();
            while (readThreads.get() != 1) {}
        }

        public boolean tryLock() {
            return lock.tryLock();
        }

        public void unlockAndAllowSubscribe() {
            lock.unlock();
            event.set();
        }

        public void unsubscribe() {
            readThreads.decrementAndGet();
        }

        private static class ManualResetEvent {
            private final Object monitor = new Object();
            private volatile boolean open = false;

            public ManualResetEvent(boolean open) {
                this.open = open;
            }

            public ManualResetEvent() {
                this(true);
            }

            public void waitOne() throws InterruptedException {
                synchronized (monitor) {
                    while (open == false) {
                        monitor.wait();
                    }
                }
            }

            public void set() {//open
                synchronized (monitor) {
                    if(!open) {
                        open = true;
                        monitor.notifyAll();
                    }
                }
            }

            public void reset() {//closed
                open = false;
            }

            public Object getMonitor() { return  monitor; }
        }
    }


    public EngineSync getEngineSync() {
        return engineSync;
    }

    /**
     * Method for getting the next weighted random test form the test suite
     * TODO: optimize
     */
    protected static AbstractTest getNextTest(TestSuite testSuite, RunMap globalRunMap, EngineSync engineSync) throws InterruptedException {

        //If we didn't find the next test we start looking for it assuming that not all counts are done
        while (testSuite.getTests().size() != 0) {
            try {
                engineSync.subscribe();
                int randomNumber = _rnd.nextInt(testSuite.getTotalWeight());
                for (AbstractTest test : testSuite.getTests()) {
                    int testWeight = testSuite.getWeightMap().get(test);

                    long testRuns = globalRunMap.getRecord(test).getTotalRuns();
                    Long maxRuns   = testSuite.getCount(test);

                    //If max runs was exceeded for a test
                    if (null != maxRuns && testRuns > maxRuns) {
                        boolean lockAquired = false;
                        //Try to acquire the lock for removing the test from the suite
                        try {
                            lockAquired = engineSync.tryLock();
                            if(!lockAquired) { break; }
                            //Don't allow any threads to subscribe and wait for all subscribed threads to unsubscribe
                            engineSync.holdSubscribeAndAwaitExisting();
                            //Remove test from suite
                            testSuite.remove(test);
                            //Start looking for the test from the beginning as the total weight changed
                            break;
                        } finally {
                            if (lockAquired) {
                                //Release lock and allow subscribers
                                engineSync.unlockAndAllowSubscribe();
                            }
                        }
                    }
                    if (randomNumber < testWeight) {
                        return test;
                    }
                    randomNumber = randomNumber - testWeight;
                }
            } finally {
                engineSync.unsubscribe();
            }
        }
        return null;
    }

    private static void logGlobal(String message) {
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
