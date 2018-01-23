package com.zero.rpc.consumer.processor;


import com.zero.rpc.Response;
import com.zero.rpc.executor.ThreadPoolExecutorFactory;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.ResponseBytes;
import com.zero.transport.api.processor.ConsumerProcessor;
import com.zero.rpc.executor.ExecutorFactory;

import java.util.concurrent.Executor;

public class DefaultConsumerProcessor implements ConsumerProcessor {

    private final Executor executor;

    public DefaultConsumerProcessor() {
        this(new ThreadPoolExecutorFactory().newExecutor(ExecutorFactory.Target.CONSUMER, "consumer.executor"));
    }

    public DefaultConsumerProcessor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void handleResponse(Channel channel, ResponseBytes responseBytes) throws Exception {
        MessageTask task = new MessageTask(channel, new Response(responseBytes));
        if (executor == null) {
            task.run();
        } else {
            executor.execute(task);
        }
    }
}
