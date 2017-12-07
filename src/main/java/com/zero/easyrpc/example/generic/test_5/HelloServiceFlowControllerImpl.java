package com.zero.easyrpc.example.generic.test_5;

import com.zero.easyrpc.client.annotation.RPCService;
import com.zero.easyrpc.example.demo.HelloSerivce;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class HelloServiceFlowControllerImpl implements HelloSerivce {

    @Override
    @RPCService(responsibilityName="xiaoy",serviceName="LAOPOPO.TEST.SAYHELLO",maxCallCountInMinute = 40)
    public String sayHello(String str) {
        return "hello "+ str;
    }

}

