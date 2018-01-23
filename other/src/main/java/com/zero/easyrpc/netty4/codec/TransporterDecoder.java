package com.zero.easyrpc.netty4.codec;

import com.zero.easyrpc.common.exception.RemotingContextException;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.utils.SystemPropertyUtil;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 解码器
 */
public class TransporterDecoder extends ReplayingDecoder<TransporterDecoder.State> {
    enum State {
        HEADER_MAGIC, HEADER_TYPE, HEADER_SIGN, HEADER_ID, HEADER_BODY_LENGTH, BODY
    }

    // 协议体最大限制, 默认5M
    private static final int MAX_BODY_SIZE = SystemPropertyUtil.getInt("io.decoder.max.body.size", 1024 * 1024 * 5);

//    private static final boolean USE_COMPOSITE_BUF = SystemPropertyUtil.getBoolean("jupiter.io.decoder.composite.buf", false);

    public TransporterDecoder() {

        super(State.HEADER_MAGIC); //设置ReplayingDecoder#state()的默认返回对象
//        if (USE_COMPOSITE_BUF) {
//            setCumulator(COMPOSITE_CUMULATOR);
//        }
    }

    // 协议头
    private final Protocol header = new Protocol();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(in.readShort()); // MAGIC
                checkpoint(State.HEADER_TYPE);
            case HEADER_TYPE:
                header.setType(in.readByte());
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.setSign(in.readByte());
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                header.setId(in.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.setBodyLength(in.readInt()); // 消息体长度
                checkpoint(State.BODY);
            case BODY:
                int bodyLength = checkBodyLength(header.bodyLength());
                byte[] bytes = new byte[bodyLength];
                in.readBytes(bytes);
                out.add(Transporter.newInstance(header.id(), header.sign(), header.type(), bytes));
                break;
            default:
                break;
        }
        //checkpoint的方法作用有两个，一是改变state的值的状态，二是获取到最新的读指针的下标
        //重新调用checkpoint(State.HEADER_MAGIC)，是把state的值重新设置为初始值HEADER_MAGIC，方便下次信息的解析读取
        checkpoint(State.HEADER_MAGIC);
    }

    private int checkBodyLength(int bodyLength) throws RemotingContextException {
        if (bodyLength > MAX_BODY_SIZE) {
            throw new RemotingContextException("body of request is bigger than limit value " + MAX_BODY_SIZE);
        }
        return bodyLength;
    }

    private void checkMagic(short magic) throws RemotingContextException {
        if (Protocol.MAGIC != magic) {
            throw new RemotingContextException("magic value is not equal " + Protocol.MAGIC);
        }
    }


}
