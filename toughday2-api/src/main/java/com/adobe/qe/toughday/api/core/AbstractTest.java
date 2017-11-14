package com.adobe.qe.toughday.api.core;

import com.adobe.qe.toughday.api.annotations.Name;
import com.adobe.qe.toughday.api.annotations.ConfigArgGet;
import com.adobe.qe.toughday.api.annotations.ConfigArgSet;
import com.adobe.qe.toughday.api.core.config.GlobalArgs;
import com.adobe.qe.toughday.internal.core.UUIDTestId;
import com.adobe.qe.toughday.api.annotations.labels.NotNull;
import com.adobe.qe.toughday.api.core.benchmark.Benchmark;
import com.adobe.qe.toughday.internal.core.benckmark.BenchmarkImpl;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstract base class for all tests. Normally you would not extend this class directly, because you would
 * have to write a runner for your new type of test. Instead you should extend the existing convenience classes
 * that already have a runner.
 */
public abstract class AbstractTest {
    protected static List<Thread> extraThreads = Collections.synchronizedList(new ArrayList<Thread>());
    private static final SimpleDateFormat TIME_STAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    private @NotNull TestId id;
    private String name;
    private AbstractTest parent;
    private GlobalArgs globalArgs;
    protected File workspace;
    private Logger logger;
    private BenchmarkImpl benchmark;
    private boolean showSteps = false;

    /**
     * Constructor. Used by the core with reflections
     */
    public AbstractTest() {
        this.id = new UUIDTestId();
        this.workspace = new File("workspace");
        // create dir structure
        this.workspace.mkdirs();
        benchmark = new BenchmarkImpl();
        this.name = getClass().getSimpleName();
        if (getClass().isAnnotationPresent(Name.class)) {
            Name d = getClass().getAnnotation(Name.class);
            this.name = d.name();
        }
    }

    public static List<Thread> getExtraThreads() {
        return extraThreads;
    }

    public static void addExtraThread(Thread thread) {
        extraThreads.add(thread);
    }

    /**
     * Getter for the name of the test. It will not include the name of the parents.
     * @return by default, it will return the class name, except otherwise configured using the setter
     */
    @ConfigArgGet
    public String getName() {
        return name;
    }

    /**
     * Getter for the full name of the test. It has prefixed, in order, all the names of the parents
     */
    public String getFullName() {
        return parent != null ? parent.getFullName() + "" + getName() : getName();
    }

    /**
     * Setter for the name
     */
    @ConfigArgSet(required = false, desc = "The name of this test")
    public AbstractTest setName(String name) {
        this.name = name;
        return this;
    }

    @ConfigArgSet(required = false, defaultValue = "false", desc = "Show test steps in the aggregated publish. (They are always shown in the detailed publish)")
    public AbstractTest setShowSteps(String showTestSteps) {
        this.showSteps = Boolean.parseBoolean(showTestSteps);
        return this;
    }

    @ConfigArgGet
    public boolean getShowSteps() {
        return this.showSteps;
    }

    /**
     * Getter for the resolution between this test, ancestor tests and global args
     * of whether or not to show the steps in the aggregated publish. If anywhere in the chain, it's set to true, we publish the steps.
     * @return
     */
    public boolean getShowStepsResolved() {
        return this.getShowSteps() || (getParent() != null ? getParent().getShowStepsResolved() : getGlobalArgs().getShowSteps());
    }

    public void setWorkspace (File workspace) {
        this.workspace = workspace;
    }

    public File getWorkspace() {
        return workspace;
    }

    public Benchmark benchmark() { return this.benchmark; }

    /**
     * Getter for the id
     * @return
     */
    public final @NotNull
    TestId getId() {
        return id;
    }

    /**
     * Setter for the id. Used in the cloning process.
     * @param id
     */
    public final void setID(TestId id) {
        this.id = id;
    }

    /**
     * Getter for the parent.
     */
    public AbstractTest getParent() {
        return parent;
    }

    /**
     * Setter for the parent
     */
    public void setParent(AbstractTest parent) {
        this.parent = parent;
    }

    /**
     * Hashcode computation based on Id.
     * It is final, because all the maps in the core rely on it.
     */
    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    /**
     * Implementation of equals method based on Id.
     * @return true if it's the same TestId, false otherwise.
     * It is final, because all the maps in the core rely on it.
     */
    @Override
    public final boolean equals(Object other) {
        if(!(other instanceof AbstractTest)) {
            return false;
        }
        return ((AbstractTest)other).getId().equals(id);
    }

    /**
     * Method for replicating a test for all threads. All clones will have the same TestId.
     * @return a deep clone of this test.
     */
    public AbstractTest clone() {
        AbstractTest newInstance = newInstance();
        newInstance.setID(this.id);
        newInstance.setName(this.getName());
        newInstance.setGlobalArgs(this.getGlobalArgs());
        newInstance.logger = logger();
        newInstance.benchmark = this.benchmark.clone();
        newInstance.setShowSteps(Boolean.toString(this.getShowSteps()));
        return newInstance;
    }

    /**
     * Get the logger of this test
     * @return
     */
    private static Logger createLogger(AbstractTest test) {

        String testName = test.getFullName();
        Class clazz = test.getClass();
        String clazzLoggerName = clazz.getName() + "#" + testName;

        Level logLevel = System.getProperty("toughday.log.level") != null ? Level.valueOf(System.getProperty("toughday.log.level")) : Level.INFO;
        final String timestamp = TIME_STAMP_FORMAT.format(new Date());
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %c{1}:%L - %m%n", null, config,
                null, null, true, false, null, null);
        Appender appender = FileAppender.createAppender(String.format("logs/toughday_%s_%s.log", testName, timestamp),
                "true", "false", "File", "true", "false", "false", "-1", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig =
                LoggerConfig.createLogger("false", logLevel, clazzLoggerName, "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(clazzLoggerName, loggerConfig);
        ctx.updateLoggers();
        Logger logger = LogManager.getLogger(clazzLoggerName);
        logger.info("Created logger {}", logger);
        return logger;
    }

    public Logger logger() {
        if (this.logger == null) {
            this.logger = createLogger(this);
        }

        return this.logger;
    }

    /**
     * Setter for global args
     * @param globalArgs
     */
    public AbstractTest setGlobalArgs(GlobalArgs globalArgs) {
        this.globalArgs = globalArgs;
        for(AbstractTest child : getChildren()) {
            child.setGlobalArgs(globalArgs);
        }
        return this;
    }

    /**
     * Getter for global args. It will return "null" if called from the constructor of the subclass.
     * If you rely on informations from global arguments to instantiate objects in tests, you should use
     * lazy instantiation for those objects.
     * @return
     */
    public GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    /**
     * Getter for the children of this test.
     * @return a list with all children of this test. Must not return null, instead should return an empty list.
     */
    public abstract List<AbstractTest> getChildren();

    /**
     * Determines if children are going to be benchmarked  individually
     * @return by default, returns true
     */
    public boolean includeChildren() { return true; }

    /**
     * Specifies what type of runner knows how this test should be ran and benchmarked.
     * @return runner class
     */
    public abstract Class<? extends AbstractTestRunner> getTestRunnerClass();

    /**
     * Creates a new instance of this test, with all the parameters already set.
     * @return a new, already configured instance of this test.
     */
    public abstract AbstractTest newInstance();

    protected void communicate(String key, Object message) {
        if(parent != null)
            parent.communicate(key, message);
    }

    protected <T> T getCommunication(String key, T defaultValue) {
        if(parent != null) {
            return (T) parent.getCommunication(key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Determine is the current test is an ancestor of the other one.
     * Ancestor means that the other test instance is either equal or is somewhere in the child hierarchy of this test.
     * @param test the other test instance
     * @return true if the current test is an ancestor
     */
    public boolean isAncestorOf(AbstractTest test) {
        AbstractTest p = test;
        while(p != null) {
            if(this.equals(p)) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }
}
