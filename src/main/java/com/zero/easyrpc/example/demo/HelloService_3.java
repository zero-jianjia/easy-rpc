package com.zero.easyrpc.example.demo;


import com.zero.easyrpc.client.annotation.RPCService;

public class HelloService_3 implements HelloSerivce {

	@Override
	@RPCService(responsibilityName = "xiaoy", serviceName = "LAOPOPO.TEST.SAYHELLO", weight = 5)
	public String sayHello(String str) {
		return "hello_3";
	}

}
