package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.netty4.util.ConnectionUtils;
import com.zero.easyrpc.netty4.model.RequestProcessor;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zero.easyrpc.common.protocal.Protocol.RPC_REQUEST;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class DefaultProviderRPCProcessor implements RequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderRPCProcessor.class);

    private DefaultProvider defaultProvider;

    public DefaultProviderRPCProcessor(DefaultProvider defaultProvider) {
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
            case RPC_REQUEST:
                //这边稍微特殊处理一下，可以返回null,我们不需要叫外层代码帮我们writeAndFlush 发出请求，因为我们持有channel，这样做rpc可以更加灵活一点
                this.defaultProvider.handlerRPCRequest(request,ctx.channel());
                break;
        }
        return null;
    }

}
