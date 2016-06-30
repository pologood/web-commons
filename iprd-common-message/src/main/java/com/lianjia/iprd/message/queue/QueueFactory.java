package com.lianjia.iprd.message.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fengxiao on 16/3/3.
 */
public class QueueFactory {
    private final static Logger logger = LoggerFactory.getLogger(QueueFactory.class);

    private QueueFactory() {
    }

    private static QueueFactory instance = null;
    private static final int CAPACITY = 100000;
    private final ConcurrentHashMap<String, Queue> queueMap = new ConcurrentHashMap();

    public static QueueFactory getInstance() {
        if (instance == null) {
            synchronized (logger) {
                if (instance == null) {
                    instance = new QueueFactory();
                }
            }
        }
        return instance;
    }

    /**
     * get blocking queue by specified topic
     */
    public <T> BlockingQueue<T> getBlockingQueueByTopic(String topic, Class<T> clazz) {
        Queue<T> queue = queueMap.get(topic);
        if (queue == null) {
            BlockingQueue linkedBlockingQueue = (BlockingQueue) Proxy.newProxyInstance(BlockingQueue.class.
                    getClassLoader(), new Class[]{BlockingQueue.class}, new QueueHandler().
                    bind(new LinkedBlockingQueue<T>(CAPACITY)).bindTopic(topic));
            queueMap.put(topic, linkedBlockingQueue);
        }
        return (BlockingQueue) queueMap.get(topic);
    }

    /**
     * shutdown and save all messages in queue
     */
    private void shutdown() {
        Iterator<Map.Entry<String, Queue>> iterator = queueMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Queue queue = iterator.next().getValue();
            AtomicInteger countDown = new AtomicInteger(10);

            while (queue != null && queue.size() != 0 && countDown.get() > 0) {
                try {
                    Thread.sleep(100L);
                    countDown.decrementAndGet();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                QueueFactory.getInstance().shutdown();
            }
        }));
    }

}
