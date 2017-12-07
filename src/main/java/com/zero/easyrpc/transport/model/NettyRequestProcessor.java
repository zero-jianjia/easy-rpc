package com.zero.easyrpc.transport.model;

import io.netty.channel.ChannelHandlerContext;

/**
 * Created by jianjia1 on 17/12/04.
 */
public interface NettyRequestProcessor {
    RemotingTransporter processRequest(ChannelHandlerContext ctx, RemotingTransporter request)
            throws Exception;
}
