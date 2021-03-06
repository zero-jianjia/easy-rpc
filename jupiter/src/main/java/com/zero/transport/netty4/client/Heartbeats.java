package com.zero.transport.netty4.client;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.zero.common.Protocol;

public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(Protocol.HEAD_LENGTH);
        buf.writeShort(Protocol.MAGIC);
        buf.writeByte(Protocol.HEARTBEAT); // 心跳包
        buf.writeByte(0);
        buf.writeLong(0);
        buf.writeInt(0);
        HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
    }

    /**
     * Returns the shared heartbeat content.
     */
    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}
