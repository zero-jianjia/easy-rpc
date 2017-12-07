package com.zero.easyrpc.example.generic.test_3;


import com.zero.easyrpc.client.consumer.Consumer;
import com.zero.easyrpc.client.consumer.ConsumerClient;
import com.zero.easyrpc.client.consumer.ConsumerConfig;
import com.zero.easyrpc.client.consumer.proxy.ProxyFactory;
import com.zero.easyrpc.common.exception.NoServiceException;
import com.zero.easyrpc.netty4.ClientConfig;

public class ConsumerTest {
	
	public static void main(String[] args) throws Exception  {
		
		ClientConfig registryNettyClientConfig = new ClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		ClientConfig provideClientConfig = new ClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();
		
		Consumer.SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000l)) {
			throw new NoServiceException("no service provider");
		}
		
		HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).timeoutMillis(3000l).newProxyInstance();
		
		for(int index = 0;index < 100000;index++){
			
			String str = helloService.sayHello("Lyncc");
			
			System.out.println(str);
			
			Thread.sleep(500l);
		}
		
		
	}

}
