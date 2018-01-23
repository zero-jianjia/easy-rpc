package com.zero.easyrpc.transport.api;

/**
 * Created by zero on 2018/1/17.
 */
public abstract class EasyChannelHandler implements ChannelHandler {

    public abstract Object handle(Channel channel, Object message);

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
        Object ret = handle(channel, message);
        channel.send(ret);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {

    }

    //    @Override
//    default void connected(Channel channel) throws RemotingException {
//
//    }
//
//    @Override
//    default void disconnected(Channel channel) throws RemotingException {
//
//    }
//
//    @Override
//    default void sent(Channel channel, Object message) throws RemotingException {
//
//    }
//
//    @Override
//    default void received(Channel channel, Object message) throws RemotingException {
//        Object ret = handle(channel, message);
//        channel.send(ret);
//    }
//
//    @Override
//    default  void caught(Channel channel, Throwable exception) throws RemotingException {
//
//    }
}
