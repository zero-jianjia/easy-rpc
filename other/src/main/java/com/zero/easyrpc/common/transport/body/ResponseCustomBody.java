package com.zero.easyrpc.common.transport.body;

import com.zero.easyrpc.common.exception.RemotingCommmonCustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.zero.easyrpc.common.utils.Status.OK;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class ResponseCustomBody implements ContentBody {

    private static final Logger logger = LoggerFactory.getLogger(ResponseCustomBody.class);

    private byte status = OK.value();

    private ResultWrapper resultWrapper;

    public ResponseCustomBody(byte status, ResultWrapper resultWrapper) {
        this.status = status;
        this.resultWrapper = resultWrapper;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public ResultWrapper getResultWrapper() {
        return resultWrapper;
    }

    public void setResultWrapper(ResultWrapper resultWrapper) {
        this.resultWrapper = resultWrapper;
    }

    @Override
    public void checkFields() throws RemotingCommmonCustomException {
    }

    public static class ResultWrapper {

        private Object result;
        private String error;

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    public Object getResult() {

        if(status == OK.value()){
            return getResultWrapper().getResult();
        }else{
            logger.warn("get result occor exception [{}]",getResultWrapper().getError());
            return null;
        }
    }

}
