package com.lianjia.iprd.message.counter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SlotBasedCounter {
    private ConcurrentHashMap<String, long[]> objCounter = new ConcurrentHashMap<String, long[]>();
    private final int numSlots;

    public SlotBasedCounter(int numSlots) {
        if (numSlots <= 0) {
            throw new IllegalArgumentException("Number of slots must be greater than zero (you requested " + numSlots + ")");
        }
        this.numSlots = numSlots;
    }

    public void incrementCount(String obj, int slot) {
        long[] counts = objCounter.get(obj);
        if (counts == null) {
            counts = new long[this.numSlots];
            objCounter.put(obj, counts);
        }
        counts[slot]++;
    }

    public long getCount(String obj, int slot) {
        long[] counts = objCounter.get(obj);
        if (counts == null) {
            return 0;
        } else {
            return counts[slot];
        }
    }

    public Map<String, Long> getCounts() {
        Map<String, Long> result = new HashMap<String, Long>();
        for (String obj : objCounter.keySet()) {
            result.put(obj, computeTotalCount(obj));
        }
        return result;
    }

    private long computeTotalCount(String obj) {
        long[] curr = objCounter.get(obj);
        long total = 0;
        for (long l : curr) {
            total += l;
        }
        return total;
    }

    /**
     * Reset the slot count of any tracked objects to zero for the given slot.
     *
     * @param slot
     */
    public void wipeSlot(int slot) {
        for (String obj : objCounter.keySet()) {
            resetSlotCountToZero(obj, slot);
        }
    }

    private void resetSlotCountToZero(String obj, int slot) {
        long[] counts = objCounter.get(obj);
        counts[slot] = 0;
    }

    private boolean shouldBeRemovedFromCounter(String obj) {
        return computeTotalCount(obj) == 0;
    }

    /**
     * Remove any object from the counter whose total count is zero (to free up memory).
     */
    public void wipeZeros() {
        Set<String> objToBeRemoved = new HashSet<String>();
        for (String obj : objCounter.keySet()) {
            if (shouldBeRemovedFromCounter(obj)) {
                objToBeRemoved.add(obj);
            }
        }
        for (String obj : objToBeRemoved) {
            objCounter.remove(obj);
        }
    }
}