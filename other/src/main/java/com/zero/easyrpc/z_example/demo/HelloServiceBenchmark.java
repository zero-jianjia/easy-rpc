package com.zero.easyrpc.z_example.demo;


import com.zero.easyrpc.client.annotation.RPCService;

public class HelloServiceBenchmark implements HelloSerivce {

	@Override
	@RPCService(responsibilityName="xiaoy",
	serviceName="TEST.SAYHELLO",
	connCount = 4,
	isFlowController = false,
	degradeServiceDesc="默认返回hello")
	public String sayHello(String str) {
		return str;
	}
	

}
