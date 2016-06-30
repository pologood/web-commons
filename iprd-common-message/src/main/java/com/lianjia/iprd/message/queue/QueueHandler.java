package com.lianjia.iprd.message.queue;

import com.lianjia.iprd.message.counter.SlidingWindowCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by fengxiao on 16/3/3.
 */
public class QueueHandler implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Object target;
    private String topic;
    private SlidingWindowCounter counter = SlidingWindowCounter.getInstance();

    public QueueHandler bind(Object target) {
        this.target = target;
        return this;
    }

    public QueueHandler bindTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("put")) {
            logger.debug("invoke topic is {}", topic);
            long total = counter.incrementCount(topic);
            logger.debug("now recent 20 minute visit count is {}", total);

            if (total > QueueCapacity.getInstance().getCapacityByTopic(topic)) {
                return null;
            }
        }

        Object result = method.invoke(target, args);

        if (method.getName().equals("take")) {
            /**
             * to do statistic
             */
        }
        return result;
    }
}
