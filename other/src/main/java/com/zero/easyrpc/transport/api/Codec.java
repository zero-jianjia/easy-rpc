package com.zero.easyrpc.transport.api;

import java.io.IOException;

/**
 * 借助Serializer进行codec
 */
public interface Codec {

    byte[] encode(Channel channel, Object message) throws IOException;

    Object decode(Channel channel, byte[] data) throws IOException;

    enum DecodeResult {
        NEED_MORE_INPUT, SKIP_SOME_INPUT
    }

}


