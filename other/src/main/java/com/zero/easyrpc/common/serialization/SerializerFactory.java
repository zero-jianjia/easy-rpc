package com.zero.easyrpc.common.serialization;

import io.netty.util.collection.ByteObjectHashMap;
import io.netty.util.collection.ByteObjectMap;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class SerializerFactory {
    private static final ByteObjectMap<Serializer> serializers = new ByteObjectHashMap<>();

    static {
        Iterator<Serializer> all = ServiceLoader.load(Serializer.class).iterator();

        while (all.hasNext()) {
            Serializer s = all.next();
            serializers.put(s.code(), s);
        }
    }


    public static Serializer getSerializer(byte code) {
        Serializer serializer = serializers.get(code);

        if (serializer == null) {
            SerializerType type = SerializerType.parse(code);
            if (type != null) {
                throw new IllegalArgumentException("serializer implementation [" + type.name() + "] not found");
            } else {
                throw new IllegalArgumentException("unsupported serializer type with code: " + code);
            }
        }

        return serializer;
    }

    public static Serializer serializerImpl() {
        return getSerializer(SerializerType.PROTO_STUFF.value());
    }
}
