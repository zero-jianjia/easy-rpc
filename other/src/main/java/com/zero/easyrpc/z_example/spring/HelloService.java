package com.zero.easyrpc.z_example.spring;


import com.zero.easyrpc.client.annotation.RPConsumer;

public interface HelloService {

	@RPConsumer(serviceName="LAOPOPO.TEST.SAYHELLO")
	String sayHello(String str);
	
}
