package com.zero.easyrpc.example.generic.test_7;


import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.HelloService_3;

public class ProviderTest_3 {
	
	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider();

		defaultProvider.serviceListenPort(7001)    					  // 暴露服务的地址
				.publishService(new HelloService_3()) // 暴露的服务
				.start(); 											  // 启动服务

	}

}
