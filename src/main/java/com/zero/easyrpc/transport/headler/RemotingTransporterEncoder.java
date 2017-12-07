package com.zero.easyrpc.transport.headler;

import com.zero.easyrpc.common.serialization.SerializerHolder;
import com.zero.easyrpc.transport.model.RemotingTransporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.zero.easyrpc.common.protocal.Protocol.MAGIC;

/**
 * @description Netty 对{@link RemotingTransporter}的编码器
 * Created by jianjia1 on 17/12/04.
 */
public class RemotingTransporterEncoder extends MessageToByteEncoder<RemotingTransporter> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RemotingTransporter msg, ByteBuf out) throws Exception {
        doEncodeRemotingTransporter(msg, out);
    }

    private void doEncodeRemotingTransporter(RemotingTransporter msg, ByteBuf out) {
        byte[] body = SerializerHolder.serializerImpl().writeObject(msg.getCustomHeader());

        out.writeShort(MAGIC) //协议头
                .writeByte(msg.getTransporterType())// 传输类型 sign 是请求还是响应
                .writeByte(msg.getCode())          // 请求类型requestcode 表明主题信息的类型，也代表请求的类型
                .writeLong(msg.getOpaque())        //requestId
                .writeInt(body.length)             //length
                .writeBytes(body);
    }

}
