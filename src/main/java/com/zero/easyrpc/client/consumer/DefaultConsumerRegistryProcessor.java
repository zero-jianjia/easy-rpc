package com.zero.easyrpc.client.consumer;

import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zero.easyrpc.common.protocal.Protocol.CHANGE_LOADBALANCE;
import static com.zero.easyrpc.common.protocal.Protocol.SUBCRIBE_RESULT;
import static com.zero.easyrpc.common.protocal.Protocol.SUBCRIBE_SERVICE_CANCEL;

/**
 *  消费者端注册功能的主要处理逻辑
 * Created by jianjia1 on 17/12/07.
 */
public class DefaultConsumerRegistryProcessor implements Processor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConsumerRegistryProcessor.class);

    private DefaultConsumer defaultConsumer;

    public DefaultConsumerRegistryProcessor(DefaultConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    @Override
    public Transporter processRequest(ChannelHandlerContext ctx, Transporter request) throws Exception {

        if (logger.isDebugEnabled()) {
            logger.debug("receive request, {} {} {}",//
                    request.getSign(), //
                    ConnectionUtils.parseChannelRemoteAddr(ctx.channel()), //
                    request);
        }

        switch (request.getSign()) {
            case SUBCRIBE_RESULT:
                // 回复ack信息 这个也要保持幂等性，因为有可能在consumer消费成功之后发送ack信息到registry信息丢失，registry回重新发送订阅结果信息
                return this.defaultConsumer.getConsumerManager().handlerSubcribeResult(request, ctx.channel());
            case SUBCRIBE_SERVICE_CANCEL:
                // 回复ack信息
                return this.defaultConsumer.getConsumerManager().handlerSubscribeResultCancel(request, ctx.channel());
            case CHANGE_LOADBALANCE:
                // 回复ack信息
                return this.defaultConsumer.getConsumerManager().handlerServiceLoadBalance(request, ctx.channel());
        }

        return null;
    }

}
