package com.lianjia.iprd.message.queue;

import com.lianjia.iprd.common.ErrorCode;
import com.lianjia.iprd.message.common.MessageException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by fengxiao on 16/6/30.
 */
public class QueueCapacity {

    private QueueCapacity() {
    }

    private static final ConcurrentHashMap<String, Integer> queueCapacityMap = new ConcurrentHashMap<>();

    public Integer getCapacityByTopic(String topic) {
        if (queueCapacityMap.get(topic) == null) {
            queueCapacityMap.putIfAbsent(topic, Integer.MAX_VALUE);
        }
        return queueCapacityMap.get(topic);
    }

    private static class QueueCapacityHolder {
        private static final QueueCapacity queueCapacity = new QueueCapacity();
    }

    public static QueueCapacity getInstance() {
        return QueueCapacityHolder.queueCapacity;
    }

    public void config(Map<String, Integer> queueCapacityMap) {
        if (this.queueCapacityMap.size() != 0) {
            throw new MessageException(ErrorCode.MESSAGE_CAPACITY_ALREADY_EXIST);
        }
        this.queueCapacityMap.putAll(queueCapacityMap);
    }

}
