package org.zero.common.concurrent.disruptor;


import org.zero.common.util.JConstants;

/**
 * Task message dispatcher.
 *
 * jupiter
 * org.jupiter.common.concurrent.disruptor
 *
 * @author jiachun.fjc
 */
public interface Dispatcher<T> {

    int BUFFER_SIZE = 32768;
    int MAX_NUM_WORKERS = JConstants.AVAILABLE_PROCESSORS << 3;

    /**
     * Dispatch a task message.
     */
    boolean dispatch(T message);

    /**
     * Shutdown
     */
    void shutdown();
}
