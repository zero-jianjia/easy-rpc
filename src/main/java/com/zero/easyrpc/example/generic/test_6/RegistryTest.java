package com.zero.easyrpc.example.generic.test_6;

import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.common.rpc.ServiceReviewState;
import com.zero.easyrpc.registry.base.DefaultRegistryServer;
import com.zero.easyrpc.registry.base.RegistryServerConfig;
import com.zero.easyrpc.netty4.ServerConfig;
import io.netty.util.internal.ConcurrentSet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;


public class RegistryTest {
	
	private static final Logger logger = LoggerFactory.getLogger(RegistryTest.class);
	
	private static DefaultRegistryServer defaultRegistryServer;
	
	public static void main(String[] args) {
		
		Thread t = new Thread(new RegistryTest1Scanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();
        
		ServerConfig config = new ServerConfig();
		RegistryServerConfig registryServerConfig = new RegistryServerConfig();
		
		registryServerConfig.setDefaultLoadBalanceStrategy(LoadBalanceStrategy.RANDOM);
		registryServerConfig.setDefaultReviewState(ServiceReviewState.PASS_REVIEW);
		//注册中心的端口号
		config.setListenPort(18010);
		defaultRegistryServer = new DefaultRegistryServer(config,registryServerConfig);
		defaultRegistryServer.start();
		
	}
	
	
	private static class RegistryTest1Scanner implements Runnable {

        @Override
        public void run() {
        	
            for (;;) {
                try {
                	logger.info("统计中");
                	Thread.sleep(10000);
                	ConcurrentMap<String, ConcurrentMap<RegisterMeta.Address, RegisterMeta>>  concurrentMap = defaultRegistryServer.getProviderManager().getGlobalRegisterInfoMap();
                    if(null != concurrentMap){
                    	for(String serviceName:concurrentMap.keySet()){
                    		ConcurrentMap<RegisterMeta.Address, RegisterMeta> map = concurrentMap.get(serviceName);
                    		if(map != null){
                    			for(RegisterMeta.Address address : map.keySet()){
                    				logger.info("serviceName [{}] address [{}] and detail [{}]",serviceName,address,map.get(address).toString());
                    			}
                    		}
                    	}
                    }
                    
                    ConcurrentMap<RegisterMeta.Address, ConcurrentSet<String>>  serviceMap = defaultRegistryServer.getProviderManager().getGlobalServiceMetaMap();
                    if(null != serviceMap){
                    	for(RegisterMeta.Address address : serviceMap.keySet()){
                    		if(null != serviceMap.get(address)){
                    			for(String str : serviceMap.get(address)){
                    				logger.info("address [{}] provider serivcename [{}]",address,str);
                    			}
                    		}
                    	}
                    }
                } catch (Throwable t) {
                    logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
                }
            }
        }
    }

}
