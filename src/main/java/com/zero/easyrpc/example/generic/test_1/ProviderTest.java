package com.zero.easyrpc.example.generic.test_1;


import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.ByeServiceImpl;
import com.zero.easyrpc.example.demo.HelloSerivceImpl;
import com.zero.easyrpc.netty4.ClientConfig;
import com.zero.easyrpc.netty4.ServerConfig;

public class ProviderTest {

	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new ClientConfig(), new ServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(8899) // 暴露服务的端口
				.publishService(new HelloSerivceImpl(), new ByeServiceImpl()) // 暴露的服务
				.start(); // 启动服务

	}

}
