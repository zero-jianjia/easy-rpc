package com.zero.easyrpc.example.provider;

import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.ByeServiceImpl;
import com.zero.easyrpc.example.demo.HelloSerivceImpl;
import com.zero.easyrpc.transport.netty.NettyClientConfig;
import com.zero.easyrpc.transport.netty.NettyServerConfig;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class ProviderTest {
    public static void main(String[] args) throws InterruptedException, RemotingException {

        DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());


        defaultProvider.registryAddress("127.0.0.1:18010") //注册中心的地址
                .monitorAddress("127.0.0.1:19010") //监控中心的地址
                .serviceListenPort(8899) //暴露服务的地址
                .publishService(new HelloSerivceImpl(),new ByeServiceImpl()) //暴露的服务
                .start(); //启动服务

    }
}
