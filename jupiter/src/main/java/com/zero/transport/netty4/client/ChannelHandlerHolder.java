package com.zero.transport.netty4.client;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();
}
