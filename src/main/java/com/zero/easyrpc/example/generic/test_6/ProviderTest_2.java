package com.zero.easyrpc.example.generic.test_6;


import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.HelloService_2;
import com.zero.easyrpc.transport.netty.NettyClientConfig;
import com.zero.easyrpc.transport.netty.NettyServerConfig;

public class ProviderTest_2 {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(8001) // 暴露服务的端口
				.publishService(new HelloService_2()) // 暴露的服务
				.start(); // 启动服务

	}

}
