package com.zero.easyrpc.common.serialization;

import com.zero.easyrpc.common.spi.BaseServiceLoader;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class SerializerHolder {
    // SPI
    private static final Serializer serializer = BaseServiceLoader.load(Serializer.class);

    public static Serializer serializerImpl() {
        return serializer;
    }
}
