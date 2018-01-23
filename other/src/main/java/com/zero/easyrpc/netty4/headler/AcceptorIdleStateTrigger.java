package com.zero.easyrpc.netty4.headler;

import com.zero.easyrpc.common.exception.RemotingNoSighException;
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
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AcceptorIdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                throw new RemotingNoSighException("No receive heart beat");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
