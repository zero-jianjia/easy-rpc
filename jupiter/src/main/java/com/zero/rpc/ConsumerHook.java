package com.zero.rpc;


import com.zero.transport.api.channel.Channel;

/**
 * Consumer's hook.
 *
 * 客户端的钩子函数.
 *
 * 在请求发送时触发 {@link #before(Request, Channel)} 方法;
 * 在响应回来时触发 {@link #after(Response, Channel)} 方法.
 *
 */
public interface ConsumerHook {

    ConsumerHook[] EMPTY_HOOKS = new ConsumerHook[0];

    /**
     * Triggered when the request data sent to the network.
     */
    void before(Request request, Channel channel);

    /**
     * Triggered when the server returns the result.
     */
    void after(Response response, Channel channel);
}
