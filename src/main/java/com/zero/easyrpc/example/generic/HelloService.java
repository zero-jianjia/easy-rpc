package com.zero.easyrpc.example.generic;

import com.zero.easyrpc.client.annotation.RPConsumer;

/**
 * Created by jianjia1 on 17/12/07.
 */
public interface HelloService {

    @RPConsumer(serviceName="LAOPOPO.TEST.SAYHELLO")
    String sayHello(String str);

}
