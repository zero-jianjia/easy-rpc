package com.zero.easyrpc.example.benchmark;

import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.example.demo.HelloServiceBenchmark;
import com.zero.easyrpc.transport.netty.NettyClientConfig;
import com.zero.easyrpc.transport.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 性能测试的provider端
 * @time
 * @modifytime
 */
public class BenchmarkProviderTest {

	public static void main(String[] args) throws InterruptedException, RemotingException {

		DefaultProvider defaultProvider = new DefaultProvider(new NettyClientConfig(), new NettyServerConfig());

		defaultProvider.registryAddress("127.0.0.1:18010") // 注册中心的地址
				.serviceListenPort(8899) // 暴露服务的地址
				.publishService(new HelloServiceBenchmark()) // 暴露的服务
				.start(); // 启动服务

	}

}