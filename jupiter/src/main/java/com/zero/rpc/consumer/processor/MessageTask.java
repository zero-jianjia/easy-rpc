package com.zero.rpc.consumer.processor;


import com.zero.rpc.Response;
import com.zero.serialization.java.JavaSerializer;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import com.zero.rpc.consumer.future.DefaultInvokeFuture;
import org.zero.rpc.exception.JupiterSerializationException;
import com.zero.rpc.model.ResultWrapper;
import com.zero.serialization.api.Serializer;
import com.zero.transport.Status;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.ResponseBytes;

import static org.zero.common.util.StackTraceUtil.stackTrace;

public class MessageTask implements Runnable {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageTask.class);

    private final Channel channel;
    private final Response response;

    private Serializer serializer = new JavaSerializer();

    public MessageTask(Channel channel, Response response) {
        this.channel = channel;
        this.response = response;
    }

    @Override
    public void run() {
        // stack copy
        final Response _response = response;
        final ResponseBytes _responseBytes = _response.responseBytes();

        byte[] bytes = _responseBytes.getBytes();
        _responseBytes.nullBytes();

        ResultWrapper wrapper;
        try {
            wrapper = serializer.readObject(bytes, ResultWrapper.class);
        } catch (Throwable t) {
            logger.error("Deserialize object failed: {}, {}.", channel.remoteAddress(), stackTrace(t));

            _response.status(Status.DESERIALIZATION_FAIL);
            wrapper = new ResultWrapper();
            wrapper.setError(new JupiterSerializationException(t));
        }
        _response.result(wrapper);

        DefaultInvokeFuture.received(channel, _response);
    }
}
