package com.zero.easyrpc.transport;


import com.zero.easyrpc.transport.api.MessageHandler;
import com.zero.easyrpc.rpc.Request;

/**
 * 
 * heartbeat的消息保持和正常请求的Request一致，这样以便更能反应service端的可用情况
 * 
 *
 */
public interface HeartbeatFactory {

    /**
     * 创建心跳包
     * 
     * @return
     */
    Request createRequest();

    /**
     * 包装 handler，支持心跳包的处理
     * 
     * @param handler
     * @return
     */
    MessageHandler wrapMessageHandler(MessageHandler handler);
}
