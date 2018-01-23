package org.zero.easyrpc.transport.netty.handler;


import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.utils.Reflects;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.zero.easyrpc.transport.api.BytesHolder;
import org.zero.easyrpc.transport.api.RequestBytes;
import org.zero.easyrpc.transport.api.ResponseBytes;

/**
 * <pre>
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │     8     │      4      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │           │             │
 *  │  MAGIC   Sign    Invoke Id   Body Length                   Body Content              │
 *           │       │           │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 * 消息头16个字节定长
 * = 2 // magic = (short) 0xbabe
 * + 1 // 消息标志位, 用来表示消息类型request/response/heartbeat等
 * + 8 // 消息 id, long 类型
 * + 4 // 消息体 body 长度, int 类型
 * </pre>
 *
 */
@ChannelHandler.Sharable
public class NettyEncoder extends MessageToByteEncoder<BytesHolder> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BytesHolder msg, ByteBuf out) throws Exception {
        if (msg instanceof RequestBytes) {
            doEncodeRequest((RequestBytes) msg, out);
        } else if (msg instanceof ResponseBytes) {
            doEncodeResponse((ResponseBytes) msg, out);
        } else {
            throw new IllegalArgumentException(Reflects.simpleClassName(msg));
        }
    }

    private void doEncodeRequest(RequestBytes request, ByteBuf out) {
        long invokeId = request.invokeId();
        byte[] bytes = request.getBytes();
        int length = bytes.length;

        out.writeShort(Protocol.MAGIC)
                .writeByte(Protocol.REQUEST)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }

    private void doEncodeResponse(ResponseBytes response, ByteBuf out) {
        long invokeId = response.invokeId();
        byte[] bytes = response.getBytes();
        int length = bytes.length;

        out.writeShort(Protocol.MAGIC)
                .writeByte(Protocol.RESPONSE)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }
}
