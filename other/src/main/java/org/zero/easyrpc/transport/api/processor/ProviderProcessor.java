package org.zero.easyrpc.transport.api.processor;

import com.zero.easyrpc.transport.api.Channel;
import org.zero.easyrpc.transport.api.RequestBytes;

public interface ProviderProcessor {

    /**
     * 处理正常请求
     */
    void handleRequest(Channel channel, RequestBytes request) throws Exception;
}
