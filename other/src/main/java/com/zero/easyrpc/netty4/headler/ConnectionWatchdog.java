package com.zero.easyrpc.netty4.headler;

import com.zero.easyrpc.netty4.util.ConnectionUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created by jianjia1 on 17/12/04.
 */
@ChannelHandler.Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter implements TimerTask, ChannelHandlerHolder {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionWatchdog.class);

    private final Bootstrap bootstrap;
    private final Timer timer;

    private boolean firstConnection = true;
    private volatile SocketAddress remoteAddress;

    private volatile boolean reconnect = true;
    private int attempts;

    public ConnectionWatchdog(Bootstrap bootstrap, Timer timer) {
        this.bootstrap = bootstrap;
        this.timer = timer;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        attempts = 0;
        firstConnection = true;

        logger.info("Connects with {}.", ConnectionUtils.parseChannelRemoteAddr(channel));
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("当前channel inactive 将关闭链接");
        boolean doReconnect = reconnect;
        if (doReconnect) {
            if(firstConnection){
                remoteAddress = ctx.channel().remoteAddress();
                firstConnection = false;
            }
            if (attempts < 12) {
                attempts++;
            }
            long timeout = 2 << attempts;
            logger.info("因为channel关闭所以讲进行重连~");
            timer.newTimeout(this, timeout, MILLISECONDS);
        }

        logger.warn("Disconnects with {}, address: {}, reconnect: {}.", ctx.channel(), remoteAddress, doReconnect);

        ctx.fireChannelInactive();
    }



    public void run(Timeout timeout) throws Exception {

        logger.info("进行重连~");
        ChannelFuture future;

        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(handlers());
                }
            });
            future = bootstrap.connect(remoteAddress);
        }

        future.addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture f) throws Exception {
                boolean succeed = f.isSuccess();

                logger.warn("Reconnects with {}, {}.", remoteAddress, succeed ? "succeed" : "failed");

                if (!succeed) {
                    f.channel().pipeline().fireChannelInactive();
                }
            }
        });
    }
}
