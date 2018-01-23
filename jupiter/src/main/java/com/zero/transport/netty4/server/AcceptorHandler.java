package com.zero.transport.netty4.server;


import io.netty.channel.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import com.zero.transport.Status;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.netty4.NettyChannel;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.processor.ProviderProcessor;

import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
public class AcceptorHandler extends ChannelDuplexHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AcceptorHandler.class);

    private static final AtomicInteger channelCounter = new AtomicInteger(0);

    private ProviderProcessor processor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        io.netty.channel.Channel ch = ctx.channel();

        if (msg instanceof RequestBytes) {
            Channel channel = NettyChannel.attachChannel(ch);
            try {
                processor.handleRequest(channel, (RequestBytes) msg);
            } catch (Throwable t) {
                processor.handleException(channel, (RequestBytes) msg, Status.SERVER_ERROR, t);
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        int count = channelCounter.incrementAndGet();
        logger.info("Connects of {} as the {}th channel.", ctx.channel(), count);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int count = channelCounter.getAndDecrement();
        logger.warn("Disconnects of {} as the {}th channel.", ctx.channel(), count);
    }


    public ProviderProcessor processor() {
        return processor;
    }

    public void processor(ProviderProcessor processor) {
        this.processor = processor;
    }
}
