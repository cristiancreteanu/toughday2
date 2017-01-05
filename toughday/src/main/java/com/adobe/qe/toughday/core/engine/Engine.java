package com.adobe.qe.toughday.core.engine;

import com.adobe.qe.toughday.Main;
import com.adobe.qe.toughday.core.*;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.Configuration;
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

    public static void printObject(TestSuite testSuite, PrintStream out, Object obj) throws InvocationTargetException, IllegalAccessException {
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

    private void run() throws Exception {
        LogManager.getLogger(Main.class).info("Running tests for {} seconds or until count for all tests has been reached",
                configuration.getGlobalArgs().getDuration());

        LOG.info("Test execution started at: " + getCurrentDateTime());
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                LOG.info("Test execution finished at: " + getCurrentDateTime());
                LogManager.getLogger(Main.class).info("Finished running tests");
            }
        });


        // Run the setup step of the suite
        if (testSuite.getSetupStep() != null) {
            testSuite.getSetupStep().setup();
        }
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


    /**
     * Method for getting the next weighted random test form the test suite
     * TODO: optimize
     */
    protected static AbstractTest getNextTest(TestSuite testSuite, RunMap globalRunMap) {
        int randomNumber = _rnd.nextInt(testSuite.getTotalWeight());
        AbstractTest selectedTest = null;
        for (AbstractTest test : testSuite.getTests()) {
            int testWeight = testSuite.getWeightMap().get(test);
            boolean selectTest = (randomNumber < testWeight);

            // Approximation based on how many times the test has been run globally
            long totalRuns = globalRunMap.getRecord(test).getTotalRuns();
            if (null != testSuite.getCount(test)) {
                selectTest &= totalRuns < testSuite.getCount(test);
            }
            if (selectTest) {
                selectedTest = test;
                break;
            }
            randomNumber = randomNumber - testWeight;
        }

        return selectedTest;
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
