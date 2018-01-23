package com.zero.transport.netty4.client;


import com.zero.transport.Connection;
import com.zero.transport.api.channel.ChannelGroup;
import com.zero.transport.api.processor.ConsumerProcessor;
import com.zero.transport.netty4.handler.NettyDecoder;
import com.zero.transport.netty4.handler.NettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import com.zero.transport.UnresolvedAddress;
import com.zero.transport.netty4.NettyConnection;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.zero.common.util.Preconditions.checkNotNull;

public class NettyClient extends AbstractNettyClient {

    // handlers
    private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();
    private final NettyEncoder encoder = new NettyEncoder();
    private final ConnectorHandler handler = new ConnectorHandler();

    public NettyClient() {
        super();
    }

    public NettyClient(int nWorkers) {
        super(nWorkers);
    }

    @Override
    public void withProcessor(ConsumerProcessor processor) {
        handler.processor(checkNotNull(processor, "processor"));
    }

    @Override
    public Connection connect(UnresolvedAddress address, boolean async) {

        final Bootstrap boot = bootstrap();
        final SocketAddress socketAddress = InetSocketAddress.createUnresolved(address.getHost(), address.getPort());
        final ChannelGroup group = group(address);

        // 重连watchdog
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, socketAddress, group) {

            @Override
            public ChannelHandler[] handlers() {
                return new ChannelHandler[]{
                        this,
                        new IdleStateChecker(timer, 0, 60, 0),
                        idleStateTrigger,
                        new NettyDecoder(),
                        encoder,
                        handler
                };
            }
        };

        ChannelFuture future;
        try {
            synchronized (bootstrapLock()) {
                boot.handler(new ChannelInitializer<Channel>() {

                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });

                future = boot.connect(socketAddress);
            }

            // 以下代码在synchronized同步块外面是安全的
            if (!async) {
                future.sync();
            }
        } catch (Throwable t) {
            throw new RuntimeException("connects to [" + address + "] fails", t);
        }

        return new NettyConnection(address, future) {

            @Override
            public void setReconnect(boolean reconnect) {
                if (reconnect) {
                    watchdog.start();
                } else {
                    watchdog.stop();
                }
            }
        };
    }

    @Override
    public Connection connect(UnresolvedAddress address) {
        return connect(address, false);
    }


}
