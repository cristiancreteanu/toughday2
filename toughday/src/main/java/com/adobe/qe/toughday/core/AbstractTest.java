package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.annotations.Name;
import com.adobe.qe.toughday.core.config.ConfigArgGet;
import com.adobe.qe.toughday.core.config.ConfigArgSet;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.tests.sequential.demo.DemoTest;
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
import java.util.*;

/**
 * Abstract base class for all tests. Normally you would not extend this class directly, because you would
 * have to write a runner for your new type of test. Instead you should extend the existing convenience classes
 * that already have a runner. {@link DemoTest} for a detailed example.
 */
public abstract class AbstractTest implements Comparable<AbstractTest> {
    private UUID id;
    private String name;
    private AbstractTest parent;
    private Configuration.GlobalArgs globalArgs;
    protected File workspace;
    protected static List<Thread> extraThreads = Collections.synchronizedList(new ArrayList<Thread>());

    /**
     * Constructor.
     */
    public AbstractTest() {
        this.id = UUID.randomUUID();
        this.workspace = new File("workspace");
        // create dir structure
        this.workspace.mkdirs();
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
        return parent != null ? parent.getFullName() + "." + getName() : getName();
    }

    /**
     * Setter for the name
     */
    @ConfigArgSet(required = false, defaultValue = "The class name", desc = "The name of this test")
    public AbstractTest setName(String name) {
        this.name = name;
        return this;
    }

    public void setWorkspace (File workspace) {
        this.workspace = workspace;
    }

    public File getWorkspace() {
        return workspace;
    }

    /**
     * Getter for the id
     * @return
     */
    public final UUID getId() {
        return id;
    }

    /**
     * Setter for the id. Used in the cloning process.
     * @param id
     */
    public final void setID(UUID id) {
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
     * @return true if it's the same UUID, false otherwise.
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
     * Method for replicating a test for all threads. All clones will have the same UUID.
     * @return a deep clone of this test.
     */
    public AbstractTest clone() {
        AbstractTest newInstance = newInstance();
        newInstance.setID(this.id);
        newInstance.setName(this.getName());
        newInstance.setGlobalArgs(this.getGlobalArgs());
        return newInstance;
    }

    /**
     * Get the logger of this test
     * @param clazz
     * @return
     */
    public static Logger createLogger(Class<?> clazz) {

            String name = clazz.getSimpleName();
        if (clazz.isAnnotationPresent(Name.class)) {
            Name d = clazz.getAnnotation(Name.class);
            name = d.name();
        }

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        Layout layout = PatternLayout.createLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L - %m%n", null, config,
                null, null, false, false, null, null);
        Appender appender = FileAppender.createAppender(String.format("logs/toughday_%s.log", name),
                "true", "false", "File", "true", "false", "false", "-1", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.INFO, clazz.getName(), "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(clazz.getName(), loggerConfig);
        ctx.updateLoggers();
        Logger logger = LogManager.getLogger(clazz);
        logger.info("Created logger {}", logger);
        return logger;
    }

    /**
     * Setter for global args
     * @param globalArgs
     */
    public AbstractTest setGlobalArgs(Configuration.GlobalArgs globalArgs) {
        this.globalArgs = globalArgs;
        return this;
    }

    /**
     * Getter for global args. It will return "null" if called from the constructor of the subclass.
     * If you rely on informations from global arguments to instantiate objects in tests, you should use
     * lazy instantiation for those objects.
     * @return
     */
    public Configuration.GlobalArgs getGlobalArgs() {
        return globalArgs;
    }

    @Override
    public int compareTo(AbstractTest that) {
        return this.getId().compareTo(that.getId());
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
}
