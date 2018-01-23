package com.zero.rpc.consumer.dispatcher;


import com.zero.rpc.RPCClient;
import com.zero.rpc.Request;
import com.zero.rpc.consumer.future.DefaultInvokeFuture;
import com.zero.rpc.consumer.future.InvokeFuture;
import com.zero.rpc.consumer.loadbalance.LoadBalancer;
import com.zero.rpc.model.MessageWrapper;
import com.zero.serialization.api.Serializer;
import com.zero.transport.api.channel.Channel;

public class DefaultDispatcher extends AbstractDispatcher {

    public DefaultDispatcher(RPCClient client, LoadBalancer loadBalancer, Serializer serializer) {
        super(client, loadBalancer, serializer);
    }

    @Override
    public <T> InvokeFuture<T> dispatch(Request request, Class<T> returnType) {
        // stack copy
        final Serializer _serializer = serializer();
        final MessageWrapper message = request.message();

        // 通过软负载均衡选择一个channel
        Channel channel = select(message.getMetadata());

        // 在业务线程中序列化, 减轻IO线程负担
        byte[] bytes = _serializer.writeObject(message);

        request.bytes(bytes);

        long timeoutMillis = getMethodSpecialTimeoutMillis(message.getMethodName());

        DefaultInvokeFuture<T> future = DefaultInvokeFuture.of(request.invokeId(), channel, returnType, timeoutMillis)
                .withHooks(hooks());

        return write(channel, request, future);
    }
}
