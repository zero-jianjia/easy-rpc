
package com.zero.serialization.api;

import io.netty.util.collection.ByteObjectHashMap;
import io.netty.util.collection.ByteObjectMap;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.JServiceLoader;

/**
 * Holds all serializers.
 *
 * jupiter
 * org.jupiter.serialization
 *
 * @author jiachun.fjc
 */
public final class SerializerFactory {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SerializerFactory.class);

    private static final ByteObjectMap<Serializer> serializers = new ByteObjectHashMap<>();

    static {
        Iterable<Serializer> all = JServiceLoader.load(Serializer.class);
        for (Serializer s : all) {
            serializers.put(s.code(), s);
        }
        logger.info("Supported serializers: {}.", serializers);
    }

    public static Serializer getSerializer(byte code) {
        Serializer serializer = serializers.get(code);

        if (serializer == null) {
            SerializerType type = SerializerType.parse(code);
            if (type != null) {
                throw new IllegalArgumentException("serializer implementation [" + type.name() + "] not found");
            } else {
                throw new IllegalArgumentException("unsupported serializer type of code: " + code);
            }
        }

        return serializer;
    }
}
