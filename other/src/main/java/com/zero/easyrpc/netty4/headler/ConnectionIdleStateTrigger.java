package com.zero.easyrpc.netty4.headler;

import com.zero.easyrpc.netty4.model.Heartbeats;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jianjia1 on 17/12/04.
 */
@ChannelHandler.Sharable
public class ConnectionIdleStateTrigger extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionIdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                logger.debug("Send heart beat.");
                ctx.writeAndFlush(Heartbeats.heartbeatContent());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}