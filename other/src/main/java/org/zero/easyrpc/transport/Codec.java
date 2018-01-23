package org.zero.easyrpc.transport;

import java.io.IOException;


/**
 * 借助Serializer对BytesHolder进行codec
 */
public interface Codec {

    byte[] encode(Object message) throws IOException;

    Object decode(byte[] data) throws IOException;
}