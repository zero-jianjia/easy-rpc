package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.transport.api.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {
    private Codec codec;
    private int maxContentLength = 10 * 1024 * 1024;

    public NettyDecoder() {
    }

    public NettyDecoder(Codec codec, int maxContentLength) {
        this.codec = codec;
        this.maxContentLength = maxContentLength;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
        Object msg;
        int saveReaderIndex;
        try {
            do {
                saveReaderIndex = input.readerIndex();
                try {
                    if (input.readableBytes() <= NettyContants.HEADLENGTH) {
                        break;
                    }
                    short magic = input.readShort();
                    if (magic != NettyContants.MAGIC) {
                        input.readerIndex(saveReaderIndex);
                        throw new RuntimeException("NettyDecoder transport header not support, magic: " + magic);
                    }

                    int dataLength = input.readInt();
                    if (input.readableBytes() < dataLength) {
                        input.readerIndex(saveReaderIndex);
                        break;
                    }

                    byte[] data = new byte[dataLength];
                    input.readBytes(data);
                    NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());
                    msg = codec.decode(channel, data);
                } catch (IOException e) {
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
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


}
