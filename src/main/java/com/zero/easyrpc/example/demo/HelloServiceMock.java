package com.zero.easyrpc.example.demo;

/**
 * Created by jianjia1 on 17/12/07.
 */
public class HelloServiceMock implements HelloSerivce {

    @Override
    public String sayHello(String str) {

        //直接给出默认的返回值
        return "hello";
    }

}