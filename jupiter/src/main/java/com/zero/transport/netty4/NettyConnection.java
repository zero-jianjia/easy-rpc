package com.zero.transport.netty4;


import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import com.zero.transport.Connection;
import com.zero.transport.UnresolvedAddress;

public abstract class NettyConnection extends Connection {

    private final ChannelFuture future;

    public NettyConnection(UnresolvedAddress address, ChannelFuture future) {
        super(address);
        this.future = future;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    @Override
    public void operationComplete(final Runnable callback) {
        future.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    callback.run();
                }
            }
        });
    }
}
