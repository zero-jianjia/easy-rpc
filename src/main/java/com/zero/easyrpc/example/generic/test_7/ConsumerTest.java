package com.zero.easyrpc.example.generic.test_7;


import com.zero.easyrpc.client.consumer.ConsumerClient;
import com.zero.easyrpc.client.consumer.proxy.ProxyFactory;
import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.utils.UnresolvedAddress;

public class ConsumerTest {
	
	public static void main(String[] args) throws Exception {
		
		ConsumerClient client = new ConsumerClient();

		client.start();
		
		UnresolvedAddress addresses_1 = new UnresolvedAddress("127.0.0.1", 9001);
		UnresolvedAddress addresses_2 = new UnresolvedAddress("127.0.0.1", 8001);
		UnresolvedAddress addresses_3 = new UnresolvedAddress("127.0.0.1", 7001);
		
		HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).addProviderAddress(addresses_1,addresses_2,addresses_3).loadBalance(LoadBalanceStrategy.WEIGHTINGRANDOM).timeoutMillis(3000l).newProxyInstance();
		
		for(int index = 1;index < 45;index++){
			
			Thread.sleep(1000l);
			String str = helloService.sayHello("Lyncc");
			System.out.println("当前调用的次数是：" + index);
			System.out.println(str);
		}
		
	}

}
