package com.zero.easyrpc.z_example.transport.netty4;

import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.Codec;
import com.zero.easyrpc.common.utils.ByteUtil;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by zero on 2018/1/15.
 */
public class StringCodec implements Codec {
    // magic header.
    protected static final byte MAGIC = (byte) 0xab;

    @Override
    public byte[] encode(Channel channel, Object message) throws IOException {
        byte[] body = SerializationUtils.serialize(message);
        byte[] data = new byte[body.length + 2];

        ByteUtil.short2bytes(MAGIC, data, 0);
        System.arraycopy(body, 0, data, 2, body.length);
        return data;
    }

    @Override
    public Object decode(Channel channel, byte[] data) throws IOException {
        short type = ByteUtil.bytes2short(data, 0);
        if (type != MAGIC) {
            System.out.println("error");
            return null;
        }

        byte[] b = Arrays.copyOfRange(data, 2, data.length);

        return SerializationUtils.deserialize(b);
    }
}
