package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.utils.NativeSupport;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ThreadFactory;

/**
 * Netty网络通讯端C/S端都需要实现的简单方法
 * Created by jianjia1 on 17/12/06.
 */
public interface BaseService {

    /**
     * Netty的一些参数的初始化
     */
    void init();

    /**
     * 启动Netty方法
     */
    void start();

    /**
     * 关闭Netty C/S 实例
     */
    void shutdown();

    /**
     * 注入钩子
     */
    void registerRPCHook(RPCHook rpcHook);


    default EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
        return isNativeEt() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
    }

    default boolean isNativeEt() {
        return NativeSupport.isSupportNativeET();
    }

}
