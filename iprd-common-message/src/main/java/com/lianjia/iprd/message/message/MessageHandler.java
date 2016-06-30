package com.lianjia.iprd.message.message;

import com.lianjia.iprd.message.queue.QueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by fengxiao on 16/3/4.
 */
public abstract class MessageHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private static final int MESSAGE_SIZE = 10;
    private static final long MINIMAL_SYNC_TIME = 1L;

    private BlockingQueue<T> messageQueue;
    private ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    });

    public void start(String topic, Class<T> clazz) {
        messageQueue = QueueFactory.getInstance().getBlockingQueueByTopic(topic, clazz);
        executorService.submit(new MessageConsumer());
    }

    /**
     * handle message
     */
    public abstract void handle(List<T> t) throws Throwable;

    private class MessageConsumer implements Runnable {

        /**
         * 发送暂存列表
         */
        List<T> messageList = new ArrayList<>(MESSAGE_SIZE);
        /**
         * 上次发送时间
         */
        long lastSendTime = 0L;
        /**
         * 发送次数
         */
        int sendTimes = 0;

        public void run() {
            while (true) {
                try {
                    T t = messageQueue.poll(MINIMAL_SYNC_TIME, TimeUnit.SECONDS);
                    if (t != null) {
                        messageList.add(t);
                        logger.info("save message to DB is {}", t);
                    }
                    if (canSend()) {

                        sendMessages();
                    }
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }

        /**
         * 批量发送条件：累计到10条数据 OR 距离上次发送超过一秒钟
         */
        public boolean canSend() {
            if (messageList.size() < MESSAGE_SIZE && lastSendTime + TimeUnit.SECONDS.toNanos(1L) > System.nanoTime()) {
                return false;
            }
            if (messageList.size() == 0) {
                return false;
            }
            return true;
        }

        /**
         * 发送信息，成功则状态重置, 尝试发送三次，如果三次全失败，则保存数据到日志文件，并清空状态
         */
        public void sendMessages() {
            if (sendTimes > 2) {
                for (T t : messageList) {
                    /**
                     * to do, save the error data
                     */
                    logger.error(t.toString());
                }

                reset();
                return;
            }
            try {
                logger.info("now consumer status is messageCount={}, sendTimes={}, lastSendTime={}", new Object[]{
                        messageList.size(), sendTimes, lastSendTime
                });
                handle(messageList);
                reset();
            } catch (Throwable e) {
                logger.warn(e.getMessage() + ", send times + " + (++sendTimes), e);
                sendMessages();
            }
        }

        public void reset() {
            lastSendTime = System.nanoTime();
            messageList.clear();
            sendTimes = 0;
        }

    }

}


