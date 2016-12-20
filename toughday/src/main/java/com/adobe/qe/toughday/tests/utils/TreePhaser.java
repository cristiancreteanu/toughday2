package com.adobe.qe.toughday.tests.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Phaser to syncronize creation of a tree when a new level needs to be created
 */
public class TreePhaser extends Phaser {
    public Thread mon;

    public TreePhaser() {
        super();
        this.monitor();
    }

    private int base = Integer.parseInt(DEFAULT_BASE);

    public static final String DEFAULT_BASE = "10";

    public final AtomicInteger nextChildPerLevel = new AtomicInteger(0);

    private int maxChildrenPerLevel = base;

    protected boolean onAdvance(int phase, int registeredParties) {
        // Increase the level
        // reset counter for level
        this.nextChildPerLevel.set(0);
        maxChildrenPerLevel = maxChildrenPerLevel * base;
        // Return false, never terminate phaser.
        /*if (LOG.isDebugEnabled()) LOG.debug("onAdvance. phase=%d registeredParties=%d tid=%d",
                phase, registeredParties, Thread.currentThread().getId());*/
        return false;
    }

    public int getNextNode() {
        int childNumber = this.nextChildPerLevel.getAndIncrement();
        if (childNumber >= maxChildrenPerLevel) {
            //if (LOG.isDebugEnabled()) LOG.debug("Waiting for sync. tid = " + Thread.currentThread().getId());
            this.arriveAndAwaitAdvance();
            return getNextNode();
        }
        return childNumber;
    }

    public int getLevel() {
        return this.getPhase() + 1;
    }

    public void monitor() {
        this.mon = new Thread() {
            @Override
            public void run() {
                do {
                    if (nextChildPerLevel.get() >= maxChildrenPerLevel ) {
                        arriveAndAwaitAdvance();
                    } else {
                        try {
                            sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                } while (true);
            }
        };
        this.register();
        mon.start();
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getBase() {
        return base;
    }

    public static String computeParentPath(int nextChild, int level, int base, String title, String prefix) {
        if (level == 1) {
            if (null == prefix || prefix.isEmpty()) {
                return "/";
            } else {
                return prefix.endsWith("/") ? prefix : prefix + "/";
            }
        }
        String path = Integer.toString(nextChild / base, base);
        path = StringUtils.leftPad(path, level-1, "0");
        return prefix + StringUtils.stripEnd(path.replace("", "/" + title), title);
    }

    public static String computeParentPath(int nextChild, int level, int base, String title) {
        return computeParentPath(nextChild, level, base, title, "/");
    }

    public static String computeNodeName(int nextChild, int BASE, String title) {
        return title + Integer.toString(nextChild % BASE, BASE);
    }
}
