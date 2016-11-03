package com.adobe.qe.toughday.tests.utils;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tuicu on 02/11/16.
 */
public class TreePhaser extends Phaser {
    public Thread mon;

    public TreePhaser() {
        super();
        this.monitor();
    }

    public static final int BASE = 10;

    public final AtomicInteger nextChildPerLevel = new AtomicInteger(0);

    private int maxChildrenPerLevel = BASE;

    protected boolean onAdvance(int phase, int registeredParties) {
        // Increase the level
        // reset counter for level
        this.nextChildPerLevel.set(0);
        maxChildrenPerLevel = maxChildrenPerLevel * BASE;
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

    public static String computeParentPath(int nextChild, int level) {
        if (level == 1) {
            return "/";
        }
        String path = Integer.toString(nextChild / TreePhaser.BASE, TreePhaser.BASE);
        path = StringUtils.leftPad(path, level-1, "0");
        return path.replace("", "/");
    }

    public static String computeNodeName(int nextChild) {
        return Integer.toString(nextChild % TreePhaser.BASE, TreePhaser.BASE);
    }
}
