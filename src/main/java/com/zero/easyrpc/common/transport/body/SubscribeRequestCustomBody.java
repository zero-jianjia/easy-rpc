package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;

/**
 *  消费者订阅服务的主题消息，这边做的相对简单，只要有唯一的名字控制就好
 * Created by jianjia1 on 17/12/07.
 */
public class SubscribeRequestCustomBody implements ContentBody {

    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }


}

