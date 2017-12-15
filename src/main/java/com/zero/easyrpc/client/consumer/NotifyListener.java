package com.zero.easyrpc.client.consumer;

/**
 * Created by jianjia1 on 17/12/07.
 */

import com.zero.easyrpc.common.rpc.RegisterMeta;

/**
 *
 * consumer从register注册中心获取到订阅信息之后返回的结果集
 *
 * 从注册中心拿到提供者的地址之后，netty去连接的时候是异步的的，ChannelFuture.channel这边是异步的，
 * 不知道啥时候能够 ChannelFuture.isSuccess()== true, 除非在后面增加一个Listener，当operationSuccess的时候才会周知用户，所有的动作初始化完毕了，可以直接调用
 */
public interface NotifyListener {

    /**
     * 接收到register返回的RegisterMeta的时候，去连接provider端
     * @param registerMeta
     * @param event
     */
    void notify(RegisterMeta registerMeta, NotifyEvent event);

    enum NotifyEvent {
        CHILD_ADDED,
        CHILD_REMOVED
    }
}
