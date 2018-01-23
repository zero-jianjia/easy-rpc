package org.zero.easyrpc.transport.api.processor;

import com.zero.easyrpc.transport.api.Channel;
import org.zero.easyrpc.transport.api.ResponseBytes;

public interface ConsumerProcessor {

    void handleResponse(Channel channel, ResponseBytes response) throws Exception;

}

