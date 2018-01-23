package com.zero.transport.netty4.client;


import com.zero.transport.api.ResponseBytes;
import com.zero.transport.netty4.NettyChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import com.zero.transport.api.processor.ConsumerProcessor;

import static org.zero.common.util.StackTraceUtil.stackTrace;

@ChannelHandler.Sharable
public class ConnectorHandler extends ChannelDuplexHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectorHandler.class);

    private ConsumerProcessor processor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (msg instanceof ResponseBytes) {
            try {
                processor.handleResponse(NettyChannel.attachChannel(ch), (ResponseBytes) msg);
            } catch (Throwable t) {
                logger.error("An exception was caught: {}, on {} #channelRead().", stackTrace(t), ch);
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Unexpected message type received: {}, channel: {}.", msg.getClass(), ch);
            }
        }
    }

    public ConsumerProcessor processor() {
        return processor;
    }

    public void processor(ConsumerProcessor processor) {
        this.processor = processor;
    }
}
