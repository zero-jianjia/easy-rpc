package org.zero.common.util.internal;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * jupiter
 * org.jupiter.common.util.internal
 *
 * @author jiachun.fjc
 */
public class InternalThreadLocalRunnable implements Runnable {

    private final Runnable runnable;

    private InternalThreadLocalRunnable(Runnable runnable) {
        this.runnable = checkNotNull(runnable, "runnable");
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            InternalThreadLocal.removeAll();
        }
    }

    public static Runnable wrap(Runnable runnable) {
        return runnable instanceof InternalThreadLocalRunnable ? runnable : new InternalThreadLocalRunnable(runnable);
    }
}
