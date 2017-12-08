package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.Processor;
import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

/**
 * netty服务端的一些抽象方法，类似{@link NettyClient}
 * Created by jianjia1 on 17/12/05.
 */
public interface NettyServer extends BaseService {

    Transporter invokeSync(final Channel channel, final Transporter request, final long timeoutMillis)
            throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException;

    void registerProecessor(final byte sign, final Processor processor, final ExecutorService executor);

    void registerChannelInactiveProcessor(final ChannelInactiveProcessor processor, final ExecutorService executor);

    void registerDefaultProcessor(final Processor processor, final ExecutorService executor);

    Pair<Processor, ExecutorService> getProcessorPair(final int requestCode);
}