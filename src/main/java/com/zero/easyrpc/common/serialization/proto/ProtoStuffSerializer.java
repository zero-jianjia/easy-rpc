package com.zero.easyrpc.common.serialization.proto;

import com.zero.easyrpc.common.serialization.Serializer;
import com.zero.easyrpc.common.serialization.SerializerType;
import com.zero.easyrpc.common.utils.Reflects;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protostuff的序列化/反序列化实现
 * Created by jianjia1 on 17/12/04.
 */
public class ProtoStuffSerializer extends Serializer {

    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    private static Objenesis objenesis = new ObjenesisStd(true);

    @SuppressWarnings("unchecked")
    public <T> byte[] writeObject(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }


    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(bytes, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) schemaCache.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            schemaCache.put(cls, schema);
        }
        return schema;
    }

    @Override
    public byte code() {
        return SerializerType.PROTO_STUFF.value();
    }

}
