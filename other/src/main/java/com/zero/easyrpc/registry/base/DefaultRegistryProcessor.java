package com.zero.easyrpc.registry.base;

import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.model.Processor;
import com.zero.easyrpc.netty4.Transporter;
import com.zero.easyrpc.registry.DefaultRegistry;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zero.easyrpc.common.protocal.Protocol.*;

/**
 * 注册中心的处理转换器
 */
public class DefaultRegistryProcessor implements Processor {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRegistryProcessor.class);

    private DefaultRegistry defaultRegistry;

    public DefaultRegistryProcessor(DefaultRegistry defaultRegistry) {
        this.defaultRegistry = defaultRegistry;
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
            case PUBLISH_SERVICE: // provider推送的服务信息
                return defaultRegistry.getProvidermanager().handlerRegister(request, ctx.channel()); // 要保持幂等性，同一个实例重复发布同一个服务的时候对于注册中心来说是无影响的
            case PUBLISH_CANCEL_SERVICE: // 处理服务提供者provider推送的服务取消的信息
                return this.defaultRegistry.getProvidermanager().handlerRegisterCancel(request, ctx.channel());

            case SUBSCRIBE_SERVICE: // 处理服务消费者consumer订阅服务的请求
                return this.defaultRegistry.getProvidermanager().handleSubscribe(request, ctx.channel());
            case MANAGER_SERVICE: // 处理管理者发送过来的服务管理服务
                return this.defaultRegistry.getProvidermanager().handleManager(request, ctx.channel());
        }

        return null;
    }

}
