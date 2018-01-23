package com.zero.rpc;

import com.zero.registry.api.RegistryService;
import com.zero.registry.zookeeper.ZookeeperRegistryService;
import com.zero.rpc.DefaultRPCServer;
import com.zero.rpc.RPCServer;
import com.zero.rpc.impl.TracingService1Impl;
import com.zero.rpc.impl.TracingService2Impl;
import com.zero.rpc.model.ServiceRegistry;
import com.zero.rpc.model.ServiceWrapper;
import com.zero.rpc.provider.ProviderInterceptor;
import com.zero.rpc.tracing.TraceId;
import com.zero.serialization.java.JavaSerializer;
import com.zero.transport.netty4.server.NettyServer;

import java.util.concurrent.TimeUnit;

public class RPCServerTest {
    public static void main(String[] args) {

        ServiceRegistry serviceRegistry2 = new ServiceRegistry.Builder()
                .provider(new TracingService1Impl())
                .build();
        ServiceRegistry serviceRegistry1 = new ServiceRegistry.Builder()
                .provider(new TracingService2Impl())
                .build();

        final RPCServer server = new DefaultRPCServer()
                .withSerializer(new JavaSerializer())
                .withAcceptor(new NettyServer(9000));

        server.withGlobalInterceptors(new ProviderInterceptor() {
            @Override
            public void beforeInvoke(TraceId traceId, Object provider, String methodName, Object[] args) {
                System.out.println("beforeInvoke  GlobalInterceptors");
            }

            @Override
            public void afterInvoke(TraceId traceId, Object provider, String methodName, Object[] args, Object result, Throwable failCause) {
                System.out.println("afterInvoke  GlobalInterceptors");
            }
        });

        //注册 服务到本地
        ServiceWrapper serviceWrapper1 = server.registInLocal(serviceRegistry1);
        server.registInLocal(serviceRegistry2);

        //注册中心
        RegistryService registryService = new ZookeeperRegistryService();
        server.withRegistryService(registryService);


        try {
            server.connectToRegistryServer("10.210.228.91:2381");
            server.publishAll();
            server.startAsync();

            TimeUnit.SECONDS.sleep(5);

            server.unpublish(serviceWrapper1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
