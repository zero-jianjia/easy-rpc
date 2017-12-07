package com.zero.easyrpc.example.benchmark;


import com.zero.easyrpc.common.rpc.ServiceReviewState;
import com.zero.easyrpc.registry.base.DefaultRegistryServer;
import com.zero.easyrpc.registry.base.RegistryServerConfig;
import com.zero.easyrpc.transport.netty.NettyServerConfig;

/**
 * 
 * @author BazingaLyn
 * @description 性能测试的注册中心端
 * @time
 * @modifytime
 */
public class BenchmarkRegistryTest {
	
	
	public static void main(String[] args) {
        
		NettyServerConfig config = new NettyServerConfig();
		RegistryServerConfig registryServerConfig = new RegistryServerConfig();
		registryServerConfig.setDefaultReviewState(ServiceReviewState.PASS_REVIEW);
		//注册中心的端口号
		config.setListenPort(18010);
		new DefaultRegistryServer(config,registryServerConfig).start();
		
	}
	
	

}
