package com.zero.easyrpc.z_example.benchmark;


import com.zero.easyrpc.common.rpc.ServiceReviewState;
import com.zero.easyrpc.registry.DefaultRegistry;
import com.zero.easyrpc.registry.RegistryConfig;
import com.zero.easyrpc.netty4.ServerConfig;

/**
 * 
 * @description 性能测试的注册中心端
 * @time
 * @modifytime
 */
public class BenchmarkRegistryTest {
	
	
	public static void main(String[] args) {
        
		ServerConfig config = new ServerConfig();
		RegistryConfig registryConfig = new RegistryConfig();
		registryConfig.setDefaultReviewState(ServiceReviewState.PASS_REVIEW);
		//注册中心的端口号
		config.setListenPort(18010);
		new DefaultRegistry(config, registryConfig).start();
		
	}
	
	

}
