package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;

/**
 * ack信息
 * Created by jianjia1 on 17/12/04.
 */
public class AckCustomBody implements ContentBody {

    //request请求id
    private long requestId;

    //是否消费处理成功
    private boolean success;

    public AckCustomBody(long requestId, boolean success) {
        this.requestId = requestId;
        this.success = success;
    }

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "AckCustomBody [requestId=" + requestId + ", success=" + success + "]";
    }

}
