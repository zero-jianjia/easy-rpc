package com.zero.easyrpc.registry.base;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.transport.model.RemotingTransporter;
import io.netty.channel.Channel;

/**
 * 注册中心处理provider的服务接口
 * Created by jianjia1 on 17/12/07.
 */
public interface RegistryProviderServer {


    /**
     * 处理provider发送过来的注册信息
     * @param remotingTransporter 里面的CommonCustomBody 是#PublishServiceCustomBody
     * @param channel
     * @return
     * @throws InterruptedException
     * @throws RemotingTimeoutException
     * @throws RemotingSendRequestException
     */
    RemotingTransporter handlerRegister(RemotingTransporter remotingTransporter,Channel channel) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException;
}

