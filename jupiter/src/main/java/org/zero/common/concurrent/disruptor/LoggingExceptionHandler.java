
package org.zero.common.concurrent.disruptor;


import com.lmax.disruptor.ExceptionHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Jupiter
 * org.jupiter.common.concurrent.disruptor
 *
 * @author jiachun.fjc
 */
public class LoggingExceptionHandler implements ExceptionHandler<Object> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LoggingExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception processing: {} {}, {}.", sequence, event, ex);
        }
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception during onStart(), {}.", ex);
        }
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        if (logger.isWarnEnabled()) {
            logger.warn("Exception during onShutdown(), {}.", ex);
        }
    }
}
