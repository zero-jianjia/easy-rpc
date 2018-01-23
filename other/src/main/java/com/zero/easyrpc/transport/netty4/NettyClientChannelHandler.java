package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.ChannelHandler;
import com.zero.easyrpc.transport.api.Codec;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.concurrent.ThreadPoolExecutor;

@io.netty.channel.ChannelHandler.Sharable
public class NettyClientChannelHandler extends ChannelDuplexHandler {
    private ThreadPoolExecutor threadPoolExecutor;
    private Channel channel;
    private Codec codec;

    private ChannelHandler handler;

    public NettyClientChannelHandler(Channel channel, Codec codec,
            ThreadPoolExecutor threadPoolExecutor) {
    }

    public NettyClientChannelHandler(Channel channel, Codec codec) {
        this.channel = channel;
        this.codec = codec;
    }

    public NettyClientChannelHandler(Channel channel, Codec codec,ChannelHandler handler) {
        this.channel = channel;
        this.codec = codec;
        this.handler = handler;
    }

    public NettyClientChannelHandler(ChannelHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future)
            throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        handler.disconnected(channel);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            handler.received(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            handler.sent(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel.getOrAddChannel(ctx.channel());
        ctx.fireChannelActive();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel.removeChannelIfDisconnected(ctx.channel());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        NettyChannel.removeChannelIfDisconnected(ctx.channel());
        ctx.channel().close();
    }
}