package com.zero.easyrpc.transport.netty4;


import com.zero.easyrpc.common.utils.StandardThreadExecutor;
import com.zero.easyrpc.transport.api.SharedObjectFactory;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NettyChannelFactory implements SharedObjectFactory<NettyClient> {
    private static final ExecutorService rebuildExecutorService = new StandardThreadExecutor(5, 30, 10L, TimeUnit.SECONDS, 100,
            new DefaultThreadFactory("RebuildExecutorService", true),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private NettyClient nettyClient;
    private String factoryName;

    public NettyChannelFactory(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.factoryName = "NettyChannelFactory_";
    }

    public synchronized NettyClient makeObject() {
        NettyClient newClient = new NettyClient(nettyClient.getClientConfig(), nettyClient.getCodec());
        newClient.setBootstrap(nettyClient.getBootstrap());
        try {
            newClient.doConnect();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
        return newClient;
    }

    @Override
    public boolean rebuildObject(NettyClient obj) {
        return false;
    }


}
