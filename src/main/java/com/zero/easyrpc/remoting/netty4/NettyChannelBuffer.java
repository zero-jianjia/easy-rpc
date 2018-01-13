package com.zero.easyrpc.remoting.netty4;

import com.zero.easyrpc.remoting.api.ChannelBuffer;
import io.netty.buffer.ByteBuf;

public class NettyChannelBuffer implements ChannelBuffer {

    private ByteBuf buffer;

    public NettyChannelBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public int readableBytes() {
        return buffer.readableBytes();
    }

    public void resetReaderIndex() {
        buffer.resetReaderIndex();
    }


    public void resetWriterIndex() {
        buffer.resetWriterIndex();
    }


    public int readerIndex() {
        return buffer.readerIndex();
    }


    public void readerIndex(int readerIndex) {
        buffer.readerIndex(readerIndex);
    }

    @Override
    public boolean readable() {
        return buffer.isReadable();
    }
}
