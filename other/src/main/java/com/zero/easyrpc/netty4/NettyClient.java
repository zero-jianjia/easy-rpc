package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.Processor;

import java.util.concurrent.ExecutorService;

/**
 * Netty客户端的一些特定的方法
 * Created by jianjia1 on 17/12/05.
 */
public interface NettyClient extends BaseService {

    /**
     * 发送request的请求，超时时间是timeoutMillis
     */
     Transporter invokeSync(final String address, final Transporter request, final long timeoutMillis)
            throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException, RemotingException;

    /**
     * 注入处理器
     */
    void registerProcessor(final byte sign, final Processor processor, final ExecutorService executor);

    /**
     * 注册channel inactive的处理器
     */
    void registerChannelInactiveProcessor(ChannelInactiveProcessor processor, ExecutorService executor);

    /**
     * 某个地址的长连接的channel是否可写
     * @param address
     * @return
     */
    boolean isChannelWriteable(final String address);

    /**
     * 当与server的channel inactive的时候，是否主动重连netty的server端
     */
    void setReconnect(boolean isReconnect);
}
