package com.zero.easyrpc.netty4;

import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.transport.body.ContentBody;
import org.zero.easyrpc.transport.api.BytesHolder;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络中的传输对象
 * Created by jianjia1 on 17/12/04.
 */
public class Transporter extends BytesHolder {

    private static final AtomicLong nextRequestId = new AtomicLong(0L);


    /**
     * 请求的类型, 例如 可以区分 订阅服务、发布服务 等等
     * 假设 sign == 1 代表是消费者订阅服务，则注册中心接到该对象的时候，先获取该sign，判断如果该code==1 则走订阅服务的处理分支代码
     * 假设 sign == 2 代表是提供者发布服务，则注册中心接收该对象的时候，先获取该sign，判断如果该code==2 则走发布服务的处理分支代码
     */
    private byte sign;

    private transient ContentBody content; //请求的主体内容
    private transient long timestamp;  //请求的时间戳
    private long requestId = nextRequestId.getAndIncrement(); //请求的id

    private byte type; //标识 传输对象是请求还是响应信息

    protected Transporter() {
    }

    /**
     * 创建一个请求传输对象
     * @param code 请求的类型
     * @param content 请求的正文
     * @return
     */
    public static Transporter createRequestTransporter(byte code, ContentBody content) {
        Transporter transporter = new Transporter();
        transporter.setSign(code);
        transporter.content = content;
        transporter.type = Protocol.REQUEST;
        return transporter;
    }

    /**
     * 创建一个响应传输对象
     * @param code 响应对象的类型
     * @param content 响应对象的正文
     * @param requestId 此响应对象对应的请求对象的id
     * @return
     */
    public static Transporter createResponseTransporter(byte code, ContentBody content, long requestId) {
        Transporter transporter = new Transporter();
        transporter.setSign(code);
        transporter.content = content;
        transporter.setRequestId(requestId);
        transporter.type = Protocol.RESPONSE;
        return transporter;
    }

    public static Transporter newInstance(long requestId, byte sign, byte type, byte[] bytes) {
        Transporter transporter = new Transporter();
        transporter.setRequestId(requestId);
        transporter.setSign(sign);
        transporter.setType(type);
        transporter.setBytes(bytes);
        return transporter;
    }


    public byte getSign() {
        return sign;
    }

    public Transporter setSign(byte sign) {
        this.sign = sign;
        return this;
    }

    public long getRequestId() {
        return requestId;
    }

    public Transporter setRequestId(long requestId) {
        this.requestId = requestId;
        return this;
    }

    public ContentBody getContent() {
        return content;
    }

    public Transporter setContent(ContentBody content) {
        this.content = content;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Transporter setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public byte getType() {
        return type;
    }

    public Transporter setType(byte type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "Transporter{" +
                "sign=" + sign +
                ", content=" + content +
                ", timestamp=" + timestamp +
                ", requestId=" + requestId +
                ", type=" + type +
                '}';
    }
}
