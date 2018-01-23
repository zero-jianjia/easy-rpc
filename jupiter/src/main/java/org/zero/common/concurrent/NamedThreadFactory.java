package org.zero.common.concurrent;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.internal.InternalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * Named thread factory.
 *
 * jupiter
 * org.jupiter.common.util
 *
 * @author jiachun.fjc
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NamedThreadFactory.class);

    private final AtomicInteger id = new AtomicInteger();
    private final String name;
    private final boolean daemon;
    private final int priority;
    private final ThreadGroup group;

    public NamedThreadFactory(String name) {
        this(name, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String name, boolean daemon) {
        this(name, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String name, int priority) {
        this(name, false, priority);
    }

    public NamedThreadFactory(String name, boolean daemon, int priority) {
        this.name = name + " #";
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        checkNotNull(r, "runnable");

        String name2 = name + id.getAndIncrement();

        Thread t = new InternalThread(group, r, name2);

        try {
            if (t.isDaemon() != daemon) {
                t.setDaemon(daemon);
            }

            if (t.getPriority() != priority) {
                t.setPriority(priority);
            }
        } catch (Exception ignored) { /* doesn't matter even if failed to set. */ }

        logger.debug("Creates new {}.", t);

        return t;
    }

    public ThreadGroup getThreadGroup() {
        return group;
    }
}
