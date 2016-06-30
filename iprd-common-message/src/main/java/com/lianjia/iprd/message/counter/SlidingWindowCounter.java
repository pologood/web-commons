package com.lianjia.iprd.message.counter;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 统计20分钟内的访问量,每2分钟更新一次数据
 */
public class SlidingWindowCounter {

    private static final Logger logger = Logger.getLogger(SlidingWindowCounter.class);

    private static SlidingWindowCounter instance = null;
    private SlotBasedCounter objCounter;
    private int headSlot;
    private int tailSlot;
    private Map<String, Long> keyCounter = new HashMap<String, Long>();
    private static final int COUNT_PERIOD = 20;
    private static final int WINDOW_LENGTH_IN_SLOTS = 10;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    private SlidingWindowCounter() {
        this.objCounter = new SlotBasedCounter(WINDOW_LENGTH_IN_SLOTS);

        this.headSlot = 0;
        this.tailSlot = slotAfter(headSlot);

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                advanceWindow();
            }
        }, 0L, COUNT_PERIOD / WINDOW_LENGTH_IN_SLOTS, TimeUnit.MINUTES);

    }

    public static SlidingWindowCounter getInstance() {
        if (instance == null) {
            synchronized (logger) {
                if (instance == null) {
                    instance = new SlidingWindowCounter();
                }
            }
        }
        return instance;
    }

    public long incrementCount(String key) {
        objCounter.incrementCount(key, headSlot);
        Long count = keyCounter.get(key);
        return count == null ? 0L : count;
    }

    /**
     * Return the current (total) counts of all tracked objects, then advance the window.
     * <p/>
     * Whenever this method is called, we consider the counts of the current sliding window to be available to and
     * successfully processed "upstream" (i.e. by the caller). Knowing this we will start counting any subsequent
     * objects within the next "chunk" of the sliding window.
     *
     * @return The current (total) counts of all tracked objects.
     */
    public void advanceWindow() {
        keyCounter = objCounter.getCounts();
        objCounter.wipeZeros();
        objCounter.wipeSlot(tailSlot);
        advanceHead();
    }

    private void advanceHead() {
        headSlot = tailSlot;
        tailSlot = slotAfter(tailSlot);
    }

    private int slotAfter(int slot) {
        return (slot + 1) % WINDOW_LENGTH_IN_SLOTS;
    }
}