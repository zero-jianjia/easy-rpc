package com.zero.easyrpc.transport.model;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理channel关闭或者inactive的状态的时候的改变
 * Created by jianjia1 on 17/12/04.
 */
public interface NettyChannelInactiveProcessor {
    void processChannelInactive(ChannelHandlerContext ctx) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException;
}
