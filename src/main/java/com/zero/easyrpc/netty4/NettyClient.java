package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.exception.RemotingSendRequestException;
import com.zero.easyrpc.common.exception.RemotingTimeoutException;
import com.zero.easyrpc.netty4.model.ChannelInactiveProcessor;
import com.zero.easyrpc.netty4.model.RequestProcessor;

import java.util.concurrent.ExecutorService;

/**
 * Netty客户端的一些特定的方法
 * Created by jianjia1 on 17/12/05.
 */
public interface NettyClient extends BaseService {

    /**
     * 发送request的请求，调用超时时间是timeoutMillis
     */
     Transporter invokeSync(final String address, final Transporter request, final long timeoutMillis)
            throws RemotingTimeoutException, RemotingSendRequestException, InterruptedException, RemotingException;

    /**
     * 注入处理器，例如某个Netty的Client实例，这个实例是Consumer端的，它需要处理订阅返回的信息
     * 假如订阅的requestCode 是100，那么给定requestCode特定的处理器processorA,且指定该处理器的线程执行器是executorA
     * 这样做的好处就是业务逻辑很清晰，什么样的业务请求对应特定的处理器
     * 一般场景下，不是高并发的场景下，executor是可以复用的，这样减少线程上下文的切换
     * @param requestCode
     * @param processor
     * @param executor
     */
    void registerProcessor(final byte requestCode, final RequestProcessor processor, final ExecutorService executor);


    /**
     * 注册channel inactive的处理器
     * @param processor
     * @param executor
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
     * @param isReconnect
     */
    void setReconnect(boolean isReconnect);
}
