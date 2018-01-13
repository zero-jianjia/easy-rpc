package com.zero.easyrpc.remoting.netty4;

import com.zero.easyrpc.remoting.api.Channel;
import com.zero.easyrpc.remoting.api.ChannelBuffer;
import com.zero.easyrpc.remoting.api.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {
    private Codec codec;
    private Channel channel;
    private int maxContentLength = 0;

    public NettyDecoder() {
    }

    public NettyDecoder(Codec codec, Channel channel, int maxContentLength) {
        this.codec = codec;
        this.channel = channel;
        this.maxContentLength = maxContentLength;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {

        ChannelBuffer message = new NettyChannelBuffer(input);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel());

        Object msg;
        int saveReaderIndex;
        try {
            do {
                saveReaderIndex = message.readerIndex();
                try {
                    msg = codec.decode(channel, message);
                } catch (IOException e) {
                    throw e;
                }
                if (msg == Codec.DecodeResult.NEED_MORE_INPUT) {
                    message.readerIndex(saveReaderIndex);
                    break;
                } else {
                    if (saveReaderIndex == message.readerIndex()) {
                        throw new IOException("Decode without read data.");
                    }
                    if (msg != null) {
                        out.add(msg);
                    }
                }
            } while (message.readable());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        if (in.readableBytes() <= MotanConstants.NETTY_HEADER) {
//            return;
//        }
//
//        in.markReaderIndex();
//        short type = in.readShort();
//        if (type != MotanConstants.NETTY_MAGIC_TYPE) {
//            in.resetReaderIndex();
//            throw new MotanFrameworkException("NettyDecoder transport header not support, type: " + type);
//        }
//        in.skipBytes(1);
//        int rpcVersion = (in.readByte() & 0xff) >>> 3;
//        switch (rpcVersion) {
//            case 0:
//                decodeV1(ctx, in, out);
//                break;
//            case 1:
//                decodeV2(ctx, in, out);
//                break;
//            default:
//                decodeV2(ctx, in, out);
//        }
//    }
//@Override
//public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//    if (in.readableBytes() < 4) {
//        return;
//    }
//    in.markReaderIndex();
//    int dataLength = in.readInt();
//    if (dataLength < 0) {
//        ctx.close();
//    }
//    if (in.readableBytes() < dataLength) {
//        in.resetReaderIndex();
//        return;	// fix 1024k buffer splice limix
//    }
//    byte[] data = new byte[dataLength];
//    in.readBytes(data);
//
//    Object obj = serializer.deserialize(data, genericClass);
//    out.add(obj);
//}


}
