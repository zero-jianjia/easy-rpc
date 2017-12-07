package com.zero.easyrpc.example.generic.test_6;


import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.HelloService_3;
import com.zero.easyrpc.netty4.ClientConfig;
import com.zero.easyrpc.netty4.ServerConfig;

public class ProviderTest_3 {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new ClientConfig(), new ServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(7001) // 暴露服务的端口
				.publishService(new HelloService_3()) // 暴露的服务
				.start(); // 启动服务

	}

}
