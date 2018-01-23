package com.zero.easyrpc.netty4.codec;

import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 编码器
 * Created by jianjia1 on 17/12/04.
 */
public class TransporterEncoder extends MessageToByteEncoder<Transporter> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Transporter msg, ByteBuf out) throws Exception {

        byte[] body = SerializerFactory.serializerImpl().writeObject(msg.getContent());

        out.writeShort(Protocol.MAGIC)               // 协议头
                .writeByte(msg.getType())            // 传输类型 type 是请求还是响应
                .writeByte(msg.getSign())            // 请求类型requestcode 表明主题信息的类型，也代表请求的类型
                .writeLong(msg.getRequestId())       // requestId
                .writeInt(body.length)               // length
                .writeBytes(body);
    }
}