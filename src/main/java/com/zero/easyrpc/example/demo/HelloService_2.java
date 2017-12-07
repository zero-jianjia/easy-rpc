package com.zero.easyrpc.example.demo;


import com.zero.easyrpc.client.annotation.RPCService;

public class HelloService_2 implements HelloSerivce {

	@Override
	@RPCService(responsibilityName = "xiaoy", serviceName = "LAOPOPO.TEST.SAYHELLO", weight = 90)
	public String sayHello(String str) {
		return "hello_2";
	}

}
