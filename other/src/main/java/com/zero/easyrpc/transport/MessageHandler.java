
package com.zero.easyrpc.transport;


import com.zero.easyrpc.transport.api.Channel;

/**
 * @author maijunsheng
 * @version 创建时间：2013-6-4
 * 
 */
public interface MessageHandler {

    Object handle(Channel channel, Object message);

}
