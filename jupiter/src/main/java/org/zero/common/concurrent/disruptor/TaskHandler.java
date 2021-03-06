
package org.zero.common.concurrent.disruptor;


import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.TimeoutHandler;
import com.lmax.disruptor.WorkHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Callback interface to be implemented for processing events as they become available in the RingBuffer.
 *
 * jupiter
 * org.jupiter.common.concurrent.disruptor
 *
 * @author jiachun.fjc
 */
public class TaskHandler implements
        EventHandler<MessageEvent<Runnable>>, WorkHandler<MessageEvent<Runnable>>, TimeoutHandler, LifecycleAware {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(TaskHandler.class);

    @Override
    public void onEvent(MessageEvent<Runnable> event, long sequence, boolean endOfBatch) throws Exception {
        event.getMessage().run();
    }

    @Override
    public void onEvent(MessageEvent<Runnable> event) throws Exception {
        event.getMessage().run();
    }

    @Override
    public void onTimeout(long sequence) throws Exception {
        if (logger.isWarnEnabled()) {
            logger.warn("Task timeout on: {}, sequence: {}.", Thread.currentThread().getName(), sequence);
        }
    }

    @Override
    public void onStart() {
        if (logger.isDebugEnabled()) {
            logger.debug("Task handler on start: {}.", Thread.currentThread().getName());
        }
    }

    @Override
    public void onShutdown() {
        if (logger.isDebugEnabled()) {
            logger.debug("Task handler on shutdown: {}.", Thread.currentThread().getName());
        }
    }
}
