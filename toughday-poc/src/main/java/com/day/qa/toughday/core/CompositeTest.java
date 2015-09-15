package com.day.qa.toughday.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tuicu on 09/09/15.
 */
public abstract class CompositeTest extends AbstractTest {
    private List<AbstractTest> children;

    public CompositeTest() {
        children = new ArrayList<>();
    }

    @Override
    public List<AbstractTest> getChildren() {
        return children;
    }

    protected AbstractTest addChild(AbstractTest child) {
        child.setParent(this);
        children.add(child);
        return this;
    }

    public AbstractTest getChild(UUID id) {
        for(AbstractTest child : children) {
            if(child.getId() == id)
                return child;
        }
        return null;
    }

    @Override
    public Class<? extends AbstractTestRunner> getTestRunnerClass() {
        return CompositeTestRunner.class;
    }

    private void setChildren(List<AbstractTest> children) {
        this.children = children;
    }

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
