package com.zero.rpc.provider;


import com.zero.rpc.RPCServer;
import com.zero.rpc.Request;
import com.zero.rpc.executor.ThreadPoolExecutorFactory;
import com.zero.rpc.model.ServiceWrapper;
import com.zero.transport.Directory;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.channel.Channel;
import com.zero.rpc.provider.flow.control.ControlResult;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.executor.ExecutorFactory;

import java.util.concurrent.Executor;

public class DefaultProviderProcessor extends AbstractProviderProcessor {

    private final RPCServer server;
    private final Executor executor;

    public DefaultProviderProcessor(RPCServer server) {
        this(server, new ThreadPoolExecutorFactory().newExecutor(ExecutorFactory.Target.PROVIDER, "provider.processor"));
    }

    public DefaultProviderProcessor(RPCServer server, Executor executor) {
        super(server.serializer());
        this.server = server;
        this.executor = executor;
    }

    @Override
    public void handleRequest(Channel channel, RequestBytes requestBytes) throws Exception {
        MessageTask task = new MessageTask(this, channel, new Request(requestBytes));
        if (executor == null) {
            task.run();
        } else {
            executor.execute(task);
        }
    }


    public ServiceWrapper lookupService(Directory directory) {
        return server.lookupService(directory);
    }

    @Override
    public ControlResult flowControl(Request request) {
        // 全局流量控制
        FlowController<Request> controller = server.globalFlowController();
        if (controller == null) {
            return ControlResult.ALLOWED;
        }
        return controller.flowControl(request);
    }

}
