package com.zero.easyrpc.example.demo;

import com.zero.easyrpc.client.annotation.RPCService;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class HelloSerivceImpl implements HelloSerivce {

    @Override
    @RPCService(responsibilityName="xiaoy",
            serviceName="LAOPOPO.TEST.SAYHELLO",
            isVIPService = false,
            isSupportDegradeService = true,
            degradeServicePath="org.laopopo.example.demo.service.HelloServiceMock",
            degradeServiceDesc="默认返回hello")
    public String sayHello(String str) {

        //真实逻辑可能涉及到查库
        return "hello "+ str;

    }

}

