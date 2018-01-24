package com.zero.transport.api.channel;

import com.zero.transport.api.FutureListener;

import java.net.SocketAddress;

public interface Channel {

    /**
     * Returns the identifier of this {@link Channel}.
     */
    String id();

    boolean isActive();

    boolean inIoThread();

    SocketAddress localAddress();

    SocketAddress remoteAddress();

    boolean isWritable();

    /**
     * Is set up automatic reconnection.
     */
    boolean isMarkedReconnect();

    boolean isAutoRead();

    void setAutoRead(boolean autoRead);

    /**
     * Requests to close this {@link Channel}.
     */
    Channel close();
    Channel close(FutureListener<Channel> listener);

    /**
     * Requests to write a message on the channel.
     */
    Channel write(Object msg);
    Channel write(Object msg, FutureListener<Channel> listener);

    /**
     * A {@link FutureListener} that closes the {@link Channel}.
     */
    FutureListener<Channel> CLOSE = new FutureListener<Channel>() {

        @Override
        public void onSuccess(Channel channel) throws Exception {
            channel.close();
        }

        @Override
        public void onFailure(Channel channel, Throwable cause) throws Exception {
            channel.close();
        }
    };
}
