package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.NetUtils;
import com.zero.easyrpc.transport.api.ChannelHandler;
import com.zero.easyrpc.transport.api.RemotingException;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@io.netty.channel.ChannelHandler.Sharable
public class NettyServerChannelHandler extends ChannelDuplexHandler {
    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>(); // <ip:port, channel>

    private ChannelHandler handler;
    private ThreadPoolExecutor threadPoolExecutor;

    public NettyServerChannelHandler() {
    }

    public NettyServerChannelHandler(ChannelHandler handler, ThreadPoolExecutor threadPoolExecutor) {
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();

        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
            handler.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future)
            throws Exception {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            if (threadPoolExecutor != null) {
                try {
                    threadPoolExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                handler.received(channel, msg);
                            } catch (RemotingException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (RejectedExecutionException rejectException) {
                    handler.received(channel, msg);
                }
            } else {
                handler.received(channel, msg);
            }
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
        try {
            handler.caught(channel, cause);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
}