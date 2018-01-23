package com.zero.easyrpc.transport.netty4;

import com.zero.easyrpc.transport.api.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Codec codec;

    public NettyEncoder(Codec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        Channel ch = ctx.channel();
        NettyChannel channel = NettyChannel.getOrAddChannel(ch);
        try {
            byte[] body = codec.encode(channel, msg);
            out.writeShort(NettyContants.MAGIC);
            out.writeInt(body.length);
            out.writeBytes(body);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ch);
        }
    }
}
