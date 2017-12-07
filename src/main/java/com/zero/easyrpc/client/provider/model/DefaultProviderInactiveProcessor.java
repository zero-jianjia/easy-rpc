package com.zero.easyrpc.client.provider.model;

import com.zero.easyrpc.client.provider.DefaultProvider;
import com.zero.easyrpc.transport.model.NettyChannelInactiveProcessor;
import io.netty.channel.ChannelHandlerContext;

/**
 * provider的netty inactive触发的事件
 * Created by jianjia1 on 17/12/04.
 */
public class DefaultProviderInactiveProcessor implements NettyChannelInactiveProcessor {

    private DefaultProvider defaultProvider;

    public DefaultProviderInactiveProcessor(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    @Override
    public void processChannelInactive(ChannelHandlerContext ctx) {
        defaultProvider.setProviderStateIsHealthy(false);
    }

}
