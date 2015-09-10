package com.day.qa.toughday.core;

import java.util.ArrayList;
import java.util.List;

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
            test.setParent(newComposite);
            clonedChildren.add(test.clone());
        }

        newComposite.setChildren(clonedChildren);
        return newComposite;
    }

}
