package com.zero.transport.netty4.handler;

import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.ResponseBytes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;
import org.zero.common.Protocol;

/**
 * <pre>
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │     8     │      4      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │           │             │
 *  │  MAGIC   Sign    Invoke Id   Body Length                   Body Content              │
 *           │       │           │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 * 消息头16个字节定长
 * = 2 // magic = (short) 0xbabe
 * + 1 // 消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型
 * + 1 // 状态位, 设置请求响应状态
 * + 8 // 消息 id, long 类型, 未来jupiter可能将id限制在48位, 留出高地址的16位作为扩展字段
 * + 4 // 消息体 body 长度, int 类型
 * </pre>
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private static final int MAX_BODY_SIZE = 1024 * 1024 * 5;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
        Object msg;
        int saveReaderIndex;
        try {
            do {
                saveReaderIndex = input.readerIndex();
                try {
                    if (input.readableBytes() <= 15) {
                        break;
                    }
                    short magic = input.readShort();
                    if (magic != Protocol.MAGIC) {
                        input.readerIndex(saveReaderIndex);
                        throw new RuntimeException("NettyDecoder transport header not support, magic: " + magic);
                    }

                    byte sign = input.readByte();
                    long id = input.readLong();

                    int dataLength = input.readInt();
                    if (input.readableBytes() < dataLength) {
                        input.readerIndex(saveReaderIndex);
                        break;
                    }

                    byte[] data = new byte[dataLength];
                    input.readBytes(data);

                    if (sign == Protocol.REQUEST) {
                        RequestBytes request = new RequestBytes(id);
                        request.setBytes(data);
                        msg = request;
                    } else if (sign == Protocol.RESPONSE) {
                        ResponseBytes response = new ResponseBytes(id);
                        response.setBytes(data);
                        msg = response;
                    } else if(sign == Protocol.HEARTBEAT) {
                        //nothing
                        msg = null;
                    }else {
                        throw new RuntimeException("NettyDecoder sign not support, sign: " + sign);
                    }
                } catch (Exception e) {
                    throw e;
                }

                if (saveReaderIndex == input.readerIndex()) {
                    throw new IOException("Decode without read data.");
                }
                if (msg != null) {
                    out.add(msg);
                }
            } while (input.isReadable());
        } finally {

        }
    }

}
