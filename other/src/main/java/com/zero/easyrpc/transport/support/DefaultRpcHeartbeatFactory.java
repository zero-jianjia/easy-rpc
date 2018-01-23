package com.zero.easyrpc.transport.support;

import com.zero.easyrpc.common.utils.RequestIdGenerator;
import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.transport.api.MessageHandler;
import com.zero.easyrpc.transport.api.RemotingException;
import com.zero.easyrpc.rpc.DefaultRequest;
import com.zero.easyrpc.rpc.DefaultResponse;
import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;
import com.zero.easyrpc.transport.HeartbeatFactory;

public class DefaultRpcHeartbeatFactory implements HeartbeatFactory {

    @Override
    public Request createRequest() {
        return getDefaultHeartbeatRequest(RequestIdGenerator.getRequestId());
    }

    @Override
    public MessageHandler wrapMessageHandler(MessageHandler handler) {
        return new HeartMessageHandleWrapper(handler);
    }

    public static Request getDefaultHeartbeatRequest(long requestId){
        HeartbeatRequest request = new HeartbeatRequest();

//        request.setRequestId(requestId);
//        request.setInterfaceName(MotanConstants.HEARTBEAT_INTERFACE_NAME);
//        request.setMethodName(MotanConstants.HEARTBEAT_METHOD_NAME);
//        request.setParamtersDesc(MotanConstants.HHEARTBEAT_PARAM);

        return request;
    }

    public static boolean isHeartbeatRequest(Object message) {
        if (!(message instanceof Request)) {
            return false;
        }
        if(message instanceof HeartbeatRequest){
            return true;
        }

        Request request = (Request) message;

//        return MotanConstants.HEARTBEAT_INTERFACE_NAME.equals(request.getInterfaceName())
//                && MotanConstants.HEARTBEAT_METHOD_NAME.equals(request.getMethodName())
//                && MotanConstants.HHEARTBEAT_PARAM.endsWith(request.getParamtersDesc());
        return false;
    }

    public static Response getDefaultHeartbeatResponse(long requestId){
        HeartbeatResponse response = new HeartbeatResponse();
        response.setRequestId(requestId);
        response.setValue("heartbeat");
        return response;
    }

    public static boolean isHeartbeatResponse(Object message){
        if(message instanceof HeartbeatResponse){
            return true;
        }
        return false;
    }


    private class HeartMessageHandleWrapper extends MessageHandler {
        private MessageHandler messageHandler;

        public HeartMessageHandleWrapper(MessageHandler messageHandler) {
            this.messageHandler = messageHandler;
        }

//        @Override
//        public Object handle(Channel channel, Object message) {
//            if (isHeartbeatRequest(message)) {
//                return getDefaultHeartbeatResponse(((Request)message).getRequestId());
//            }
//            return messageHandler.handle(channel, message);
//        }


        @Override
        public void connected(Channel channel) throws RemotingException {
        }

        @Override
        public void disconnected(Channel channel) throws RemotingException {
        }

        @Override
        public void sent(Channel channel, Object message) throws RemotingException {
        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {
        }

        @Override
        public void caught(Channel channel, Throwable exception) throws RemotingException {
        }
    }

    static class HeartbeatResponse extends DefaultResponse {}
    static class HeartbeatRequest extends DefaultRequest {}
}
