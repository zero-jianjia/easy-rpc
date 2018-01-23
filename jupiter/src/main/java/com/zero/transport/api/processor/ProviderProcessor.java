package com.zero.transport.api.processor;


import com.zero.transport.Status;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.RequestBytes;

/**
 * Provider's processor.
 *
 */
public interface ProviderProcessor {

    /**
     * 处理正常请求
     */
    void handleRequest(Channel channel, RequestBytes request) throws Exception;

    /**
     * 处理异常
     */
    void handleException(Channel channel, RequestBytes request, Status status, Throwable cause);
}
