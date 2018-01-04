package com.zero.easyrpc.common.serialization.proto;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.zero.easyrpc.common.serialization.Serializer;
import com.zero.easyrpc.common.serialization.SerializerType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by zero on 2018/1/4.
 */
public class HessianSerializer extends Serializer {

    // 目的是复用 ByteArrayOutputStream 中的 byte[]
    private static final InheritableThreadLocal<ByteArrayOutputStream> bufThreadLocal = new InheritableThreadLocal<ByteArrayOutputStream>() {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(512);
        }
    };
    @Override
    public byte code() {
        return SerializerType.HESSIAN.value();
    }

    @Override
    public <T> byte[] writeObject(T obj) {
        ByteArrayOutputStream buf = bufThreadLocal.get();
        Hessian2Output output = new Hessian2Output(buf);
        try {
            output.writeObject(obj);
            output.flush();
            return buf.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException ignored) {}

            buf.reset(); // for reuse
        }
        return null; // never get here
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(bytes, offset, length));
        try {
            Object obj = input.readObject(clazz);
            return clazz.cast(obj);
        } catch (IOException e) {
           e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException ignored) {}
        }
        return null; // never get here
    }
}
