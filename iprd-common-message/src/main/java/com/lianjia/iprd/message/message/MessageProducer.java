package com.lianjia.iprd.message.message;

import com.lianjia.iprd.message.queue.QueueFactory;

import java.util.concurrent.BlockingQueue;

/**
 * Created by fengxiao on 16/3/11.
 */
public class MessageProducer<T> {

    private BlockingQueue<T> messageQueue;

    public void produce(T t) throws InterruptedException {
        messageQueue.put(t);
    }

    public void init(String topic, Class<T> clazz) {
        this.messageQueue = QueueFactory.getInstance().getBlockingQueueByTopic(topic, clazz);
    }

}
