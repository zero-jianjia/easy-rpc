package com.zero.easyrpc.netty4.headler;

import io.netty.channel.ChannelHandler;

/**
 * Created by jianjia1 on 17/12/04.
 */
public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();

}