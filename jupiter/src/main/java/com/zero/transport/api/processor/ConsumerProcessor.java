package com.zero.transport.api.processor;


import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.ResponseBytes;

/**
 * Consumer's processor.
 */
public interface ConsumerProcessor {

    void handleResponse(Channel channel, ResponseBytes response) throws Exception;
}
