package org.zero.common.util.internal;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import static org.zero.common.util.StackTraceUtil.stackTrace;

/**
 * For the {@link Unsafe} access.
 *
 * jupiter
 * org.jupiter.common.util.internal
 *
 * @author jiachun.fjc
 */
public final class JUnsafe {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(JUnsafe.class);

    private static final Unsafe UNSAFE;

    static {
        Unsafe unsafe;
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("sun.misc.Unsafe.theUnsafe: unavailable, {}.", stackTrace(t));
            }

            unsafe = null;
        }

        UNSAFE = unsafe;
    }

    /**
     * Returns the {@link Unsafe}'s instance.
     */
    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    /**
     * Returns the system {@link ClassLoader}.
     */
    public static ClassLoader getSystemClassLoader() {
        if (System.getSecurityManager() == null) {
            return ClassLoader.getSystemClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

                @Override
                public ClassLoader run() {
                    return ClassLoader.getSystemClassLoader();
                }
            });
        }
    }

    private JUnsafe() {}
}
