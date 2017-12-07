package com.zero.easyrpc.netty4.model;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理channel关闭或者inactive的状态的时候的改变
 */
public interface ChannelInactiveProcessor {

    void processChannelInactive(ChannelHandlerContext ctx)
            throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException;

}
