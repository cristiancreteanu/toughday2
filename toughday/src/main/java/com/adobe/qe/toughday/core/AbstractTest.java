package com.adobe.qe.toughday.core;

import com.adobe.qe.toughday.core.config.ConfigArg;
import com.adobe.qe.toughday.core.config.Configuration;
import com.adobe.qe.toughday.tests.sequential.demo.DemoTest;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all tests. Normally you would not extend this class directly, because you would
 * have to write a runner for your new type of test. Instead you should extend the existing convenience classes
 * that already have a runner. {@link DemoTest} for a detailed example.
 */
public abstract class AbstractTest {
    private UUID id;
    private String name;
    private AbstractTest parent;
    private Configuration.GlobalArgs globalArgs;
    protected File workspace;

    /**
     * Constructor.
     */
    public AbstractTest() {
        this.id = UUID.randomUUID();
        this.workspace = new File("workspace");
        // create dir structure
        this.workspace.mkdirs();
    }

    /**
     * Getter for the name of the test. It will not include the name of the parents.
     * @return by default, it will return the class name, except otherwise configured using the setter
     */
    public String getSimpleName() {
        return name != null ? name : getClass().getSimpleName();
    }

    /**
     * Getter for the full name of the test. It has prefixed, in order, all the names of the parents
     */
    public String getName() {
        return parent != null ? parent.getName() + "." + getSimpleName() : getSimpleName();
    }

    /**
     * Setter for the name
     */
    @ConfigArg(required = false, defaultValue = "The class name", desc = "The name of this test")
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
        newInstance.setName(this.getSimpleName());
        newInstance.setGlobalArgs(this.getGlobalArgs());
        return newInstance;
    }

    /**
     * Setter for global args
     * @param globalArgs
     */
    public void setGlobalArgs(Configuration.GlobalArgs globalArgs) {
        this.globalArgs = globalArgs;
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

    /**
     * Getter for the children of this test.
     * @return a list with all children of this test. Must not return null, instead should return an empty list.
     */
    public abstract List<AbstractTest> getChildren();

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

}
