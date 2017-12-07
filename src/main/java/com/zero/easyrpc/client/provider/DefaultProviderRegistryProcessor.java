package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.model.RequestProcessor;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zero.easyrpc.common.protocal.Protocol.AUTO_DEGRADE_SERVICE;
import static com.zero.easyrpc.common.protocal.Protocol.DEGRADE_SERVICE;

/**
 * provider端注册的处理器
 * Created by jianjia1 on 17/12/04.
 */
public class DefaultProviderRegistryProcessor implements RequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderRegistryProcessor.class);

    private DefaultProvider defaultProvider;

    public DefaultProviderRegistryProcessor(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
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
            case DEGRADE_SERVICE:
                return this.defaultProvider.handlerDegradeServiceRequest(request,ctx.channel(),DEGRADE_SERVICE);
            case AUTO_DEGRADE_SERVICE:
                return this.defaultProvider.handlerDegradeServiceRequest(request,ctx.channel(),AUTO_DEGRADE_SERVICE);
        }
        return null;
    }

}
