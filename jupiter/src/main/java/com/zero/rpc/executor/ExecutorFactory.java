package com.zero.rpc.executor;

import java.util.concurrent.Executor;

/**
 * Executor factory.
 */
public interface ExecutorFactory  {

    Executor newExecutor(Target target, String name);

    enum Target {
        CONSUMER,
        PROVIDER
    }

    String CONSUMER_EXECUTOR_CORE_WORKERS           = "jupiter.executor.factory.consumer.core.workers";
    String PROVIDER_EXECUTOR_CORE_WORKERS           = "jupiter.executor.factory.provider.core.workers";
    String CONSUMER_EXECUTOR_MAX_WORKERS            = "jupiter.executor.factory.consumer.max.workers";
    String PROVIDER_EXECUTOR_MAX_WORKERS            = "jupiter.executor.factory.provider.max.workers";
    String CONSUMER_EXECUTOR_QUEUE_TYPE             = "jupiter.executor.factory.consumer.queue.type";
    String PROVIDER_EXECUTOR_QUEUE_TYPE             = "jupiter.executor.factory.provider.queue.type";
    String CONSUMER_EXECUTOR_QUEUE_CAPACITY         = "jupiter.executor.factory.consumer.queue.capacity";
    String PROVIDER_EXECUTOR_QUEUE_CAPACITY         = "jupiter.executor.factory.provider.queue.capacity";
    String CONSUMER_DISRUPTOR_WAIT_STRATEGY_TYPE    = "jupiter.executor.factory.consumer.disruptor.wait.strategy.type";
    String PROVIDER_DISRUPTOR_WAIT_STRATEGY_TYPE    = "jupiter.executor.factory.provider.disruptor.wait.strategy.type";
    String CONSUMER_THREAD_POOL_REJECTED_HANDLER    = "jupiter.executor.factory.consumer.thread.pool.rejected.handler";
    String PROVIDER_THREAD_POOL_REJECTED_HANDLER    = "jupiter.executor.factory.provider.thread.pool.rejected.handler";
    String EXECUTOR_AFFINITY_THREAD                 = "jupiter.executor.factory.affinity.thread";
}
