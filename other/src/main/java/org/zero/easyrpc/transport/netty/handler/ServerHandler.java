package org.zero.easyrpc.transport.netty.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.zero.easyrpc.transport.api.processor.ProviderProcessor;

public class ServerHandler extends ChannelDuplexHandler {

    private ProviderProcessor processor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

}
