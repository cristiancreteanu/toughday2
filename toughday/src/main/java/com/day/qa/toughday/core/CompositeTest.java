package com.day.qa.toughday.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tuicu on 09/09/15.
 * Convenience class for creating composite steps. Extend this class to create composite tests. See CompositeDemoTest
 * for more details.
 * Each step will be executed sequentially, in order, individually benchmarked and the whole test will be benchmarked
 * as well. You can have any number of child steps and each step can be either a simple, or a composite test. Also, the
 * steps can have different runners.
 */
public abstract class CompositeTest extends AbstractTest {
    private List<AbstractTest> children;

    /**
     * Constructor.
     */
    public CompositeTest() {
        children = new ArrayList<>();
    }

    /**
     * Getter for the children list.
     * @return a list of the children.
     */
    @Override
    public List<AbstractTest> getChildren() {
        return children;
    }

    /**
     * Method for adding a child test.
     * @param child
     * @return this test. (builder pattern)
     */
    protected AbstractTest addChild(AbstractTest child) {
        child.setParent(this);
        children.add(child);
        return this;
    }

    /**
     * Getter for a child if the UUID is known.
     * @param id
     * @return the child with the UUID given, or null if it cannot be found.
     */
    public AbstractTest getChild(UUID id) {
        for(AbstractTest child : children) {
            if(child.getId() == id)
                return child;
        }
        return null;
    }

    /**
     * Getter for the runner of the test.
     * @return CompositeTestRunner.class
     */
    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return CompositeTestRunner.class;
    }

    /**
     * Method for setting the children list. This will replace the existing list of children, without keeping the
     * previous ones.
     * @param children
     */
    private void setChildren(List<AbstractTest> children) {
        this.children = children;
    }

    /**
     * Method for cloning a composite test. The newInstance method is left abstract for the subclasses to implement.
     * @return a deep clone of this test, including its children.
     */
    @Override
    public AbstractTest clone() {
        CompositeTest newComposite = (CompositeTest) super.clone();

        List<AbstractTest> clonedChildren = new ArrayList<>();
        for(AbstractTest test : children) {
            AbstractTest newTest = test.clone();
            newTest.setParent(newComposite);
            clonedChildren.add(newTest);
        }

        newComposite.setChildren(clonedChildren);
        return newComposite;
    }

}
