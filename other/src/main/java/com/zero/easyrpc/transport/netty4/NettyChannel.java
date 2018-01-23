package com.zero.easyrpc.transport.netty4;


import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.RemotingException;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * NettyChannel.
 */
final class NettyChannel implements Channel {
    private static final ConcurrentMap<io.netty.channel.Channel, NettyChannel> channelMap = new ConcurrentHashMap<>();

    private final io.netty.channel.Channel channel;

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    private NettyChannel(io.netty.channel.Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("netty channel == null");
        }
        this.channel = channel;
    }

    static NettyChannel getOrAddChannel(io.netty.channel.Channel ch) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = channelMap.get(ch);
        if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(ch);
            if (ch.isActive()) {
                ret = channelMap.putIfAbsent(ch, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }

    static void removeChannelIfDisconnected(io.netty.channel.Channel ch) {
        if (ch != null && !ch.isActive()) {
            channelMap.remove(ch);
        }
    }

    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    public boolean isConnected() {
        return channel.isActive();
    }

    public void send(Object message, boolean sent) throws RemotingException {
        boolean success = true;
        int timeout = 1000;
        try {
            ChannelFuture future = channel.writeAndFlush(message);
            if (sent) {
                success = future.await(timeout);
            }
            Throwable cause = future.cause();
            if (cause != null) {
                throw cause;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }

        if (!success) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress()
                    + "in timeout 1000ms) limit");
        }
    }

    public void close() {
        try {
            removeChannelIfDisconnected(channel);
        } catch (Exception e) {
        }
        try {
            attributes.clear();
        } catch (Exception e) {
        }
        try {
            channel.close();
        } catch (Exception e) {
        }
    }

    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        if (value == null) { // The null value unallowed in the ConcurrentHashMap.
            attributes.remove(key);
        } else {
            attributes.put(key, value);
        }
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public void close(int timeout) {
        channel.close();
    }

    @Override
    public boolean isClosed() {
        return !channel.isActive();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channel == null) ? 0 : channel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        NettyChannel other = (NettyChannel) obj;
        if (channel == null) {
            if (other.channel != null) return false;
        } else if (!channel.equals(other.channel)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NettyChannel [channel=" + channel + "]";
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.send(message, true);
    }
}