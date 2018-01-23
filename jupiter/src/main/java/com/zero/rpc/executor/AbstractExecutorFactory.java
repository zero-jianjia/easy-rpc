package com.zero.rpc.executor;

import org.zero.common.concurrent.AffinityNamedThreadFactory;
import org.zero.common.concurrent.NamedThreadFactory;
import org.zero.common.util.JConstants;
import org.zero.common.util.SystemPropertyUtil;

import java.util.concurrent.ThreadFactory;

public abstract class AbstractExecutorFactory implements ExecutorFactory {

    protected ThreadFactory threadFactory(String name) {
        boolean affinity = SystemPropertyUtil.getBoolean(EXECUTOR_AFFINITY_THREAD, false);
        if (affinity) {
            return new AffinityNamedThreadFactory(name);
        } else {
            return new NamedThreadFactory(name);
        }
    }

    protected int coreWorkers(Target target) {
        switch (target) {
            case CONSUMER:
                return SystemPropertyUtil.getInt(CONSUMER_EXECUTOR_CORE_WORKERS, JConstants.AVAILABLE_PROCESSORS << 1);
            case PROVIDER:
                return SystemPropertyUtil.getInt(PROVIDER_EXECUTOR_CORE_WORKERS, JConstants.AVAILABLE_PROCESSORS << 4);
            default:
                throw new IllegalArgumentException(String.valueOf(target));
        }
    }

    protected int maxWorkers(Target target) {
        switch (target) {
            case CONSUMER:
                return SystemPropertyUtil.getInt(CONSUMER_EXECUTOR_MAX_WORKERS, JConstants.AVAILABLE_PROCESSORS << 3);
            case PROVIDER:
                return SystemPropertyUtil.getInt(PROVIDER_EXECUTOR_MAX_WORKERS, JConstants.AVAILABLE_PROCESSORS << 7);
            default:
                throw new IllegalArgumentException(String.valueOf(target));
        }
    }

    protected int queueCapacity(Target target) {
        switch (target) {
            case CONSUMER:
                return SystemPropertyUtil.getInt(CONSUMER_EXECUTOR_QUEUE_CAPACITY, 32768);
            case PROVIDER:
                return SystemPropertyUtil.getInt(PROVIDER_EXECUTOR_QUEUE_CAPACITY, 32768);
            default:
                throw new IllegalArgumentException(String.valueOf(target));
        }
    }
}
