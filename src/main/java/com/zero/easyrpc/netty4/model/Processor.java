package com.zero.easyrpc.netty4.model;

import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理请求
 */
public interface Processor {

    Transporter processRequest(ChannelHandlerContext ctx, Transporter request) throws Exception;

}
