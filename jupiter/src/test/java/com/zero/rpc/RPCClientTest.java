package com.zero.rpc;

import com.zero.registry.api.RegistryService;
import com.zero.registry.zookeeper.ZookeeperRegistryService;
import com.zero.rpc.consumer.ProxyFactory;
import com.zero.rpc.consumer.cluster.ClusterInvoker;
import com.zero.rpc.impl.TracingService;
import com.zero.serialization.java.JavaSerializer;
import com.zero.transport.netty4.client.NettyClient;

import java.util.concurrent.TimeUnit;

public class RPCClientTest {
    public static void main(String[] args) {

        RPCClient client = new DefaultClient("appName");
        client.withConnector(new NettyClient());
        //注册中心
        RegistryService registryService = new ZookeeperRegistryService();
        registryService.connectToRegistryServer("10.210.228.91:2381");
        client.withRegistryService(registryService);


        TracingService service = ProxyFactory
                .factory(TracingService.class)
                .version("1.0.1")
                .client(client)
                .serializer(new JavaSerializer())
                .clusterStrategy(ClusterInvoker.Strategy.FAIL_OVER)
                .failoverRetries(5)
                .newProxyInstance();


        System.out.println(service.call2("com/zero"));

        for (int i = 0; i < 5; i++) {
            System.out.println(service.call2("zero-"+i));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
