package org.zero.common.concurrent;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import net.openhft.affinity.AffinityLock;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityStrategy;
import org.zero.common.util.ClassUtil;
import org.zero.common.util.internal.InternalThread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * This is a ThreadFactory which assigns threads based the strategies provided.
 *
 * If no strategies are provided AffinityStrategies.ANY is used.
 *
 * Jupiter
 * org.jupiter.common.concurrent
 *
 * @author jiachun.fjc
 */
public class AffinityNamedThreadFactory implements ThreadFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AffinityNamedThreadFactory.class);

    static {
        // 检查是否存在slf4j, 使用Affinity必须显式引入slf4j依赖
        ClassUtil.classCheck("org.slf4j.Logger");
    }

    private final AtomicInteger id = new AtomicInteger();
    private final String name;
    private final boolean daemon;
    private final int priority;
    private final ThreadGroup group;
    private final AffinityStrategy[] strategies;
    private AffinityLock lastAffinityLock = null;

    public AffinityNamedThreadFactory(String name, AffinityStrategy... strategies) {
        this(name, false, Thread.NORM_PRIORITY, strategies);
    }

    public AffinityNamedThreadFactory(String name, boolean daemon, AffinityStrategy... strategies) {
        this(name, daemon, Thread.NORM_PRIORITY, strategies);
    }

    public AffinityNamedThreadFactory(String name, int priority, AffinityStrategy... strategies) {
        this(name, false, priority, strategies);
    }

    public AffinityNamedThreadFactory(String name, boolean daemon, int priority, AffinityStrategy... strategies) {
        this.name = "affinity." + name + " #";
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
        this.strategies = strategies.length == 0 ? new AffinityStrategy[] { AffinityStrategies.ANY } : strategies;
    }

    @Override
    public Thread newThread(final Runnable r) {
        checkNotNull(r, "runnable");

        String name2 = name + id.getAndIncrement();
        Runnable r2 = new Runnable() {

            @Override
            public void run() {
                AffinityLock al;
                synchronized (AffinityNamedThreadFactory.this) {
                    al = lastAffinityLock == null ? AffinityLock.acquireLock() : lastAffinityLock.acquireLock(strategies);
                    if (al.cpuId() >= 0) {
                        if (!al.isBound()) {
                            al.bind();
                        }
                        lastAffinityLock = al;
                    }
                }
                try {
                    r.run();
                } finally {
                    al.release();
                }
            }
        };

        Thread t = new InternalThread(group, r2, name2);

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
}
