package com.zero.easyrpc.z_example.benchmark;

import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.z_example.demo.HelloServiceBenchmark;
import com.zero.easyrpc.netty4.ClientConfig;
import com.zero.easyrpc.netty4.ServerConfig;

/**
 * 
 * @description 性能测试的provider端
 */
public class BenchmarkProviderTest {

	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new ClientConfig(), new ServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(8899) // 暴露服务的地址
				.publishService(new HelloServiceBenchmark()) // 暴露的服务
				.start(); // 启动服务

	}

}
