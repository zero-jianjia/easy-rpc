package com.zero.rpc.provider;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.ExceptionUtil;
import com.zero.rpc.Request;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.model.MessageWrapper;
import com.zero.rpc.model.ResultWrapper;
import com.zero.serialization.api.Serializer;
import com.zero.transport.Status;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.channel.FutureListener;
import com.zero.transport.api.RequestBytes;
import com.zero.transport.api.ResponseBytes;
import com.zero.transport.api.processor.ProviderProcessor;

import static org.zero.common.util.Preconditions.checkNotNull;
import static org.zero.common.util.StackTraceUtil.stackTrace;

public abstract class AbstractProviderProcessor implements ProviderProcessor, FlowController<Request> {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractProviderProcessor.class);

    private Serializer serializer;

    public AbstractProviderProcessor(Serializer serializer) {
        checkNotNull(serializer, "serializer");
        this.serializer = serializer;
    }

    @Override
    public void handleException(Channel channel, RequestBytes request, Status status, Throwable cause) {
        logger.error("An exception was caught while processing request: {}, {}.",
                channel.remoteAddress(), stackTrace(cause));

        doHandleException(channel, request.invokeId(), status.value(), cause, false);
    }

    public void handleException(Channel channel, Request request, Status status, Throwable cause) {
        logger.error("An exception was caught while processing request: {}, {}.",
                channel.remoteAddress(), stackTrace(cause));

        doHandleException(channel, request.invokeId(), status.value(), cause, false);
    }

    public void handleRejected(Channel channel, Request request, Status status, Throwable cause) {
        if (logger.isWarnEnabled()) {
            logger.warn("Service rejected: {}, {}.", channel.remoteAddress(), stackTrace(cause));
        }

        doHandleException(channel, request.invokeId(), status.value(), cause, true);
    }

    private void doHandleException(
            Channel channel, long invokeId, byte status, Throwable cause, boolean closeChannel) {

        ResultWrapper result = new ResultWrapper();
        // 截断cause, 避免客户端无法找到cause类型而无法序列化
        cause = ExceptionUtil.cutCause(cause);
        result.setError(cause);

        byte[] bytes = serializer.writeObject(result);

        ResponseBytes response = new ResponseBytes(invokeId);
        response.status(status);
        response.setBytes(bytes);

        if (closeChannel) {
            channel.write(response, Channel.CLOSE);
        } else {
            channel.write(response, new FutureListener<Channel>() {

                @Override
                public void onSuccess(Channel channel) throws Exception {
                    logger.debug("Service error message sent out: {}.", channel);
                }

                @Override
                public void onFailure(Channel channel, Throwable cause) throws Exception {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Service error message sent failed: {}, {}.", channel, stackTrace(cause));
                    }
                }
            });
        }
    }


    public MessageWrapper doSerializer(RequestBytes requestBytes) {
        byte[] bytes = requestBytes.getBytes();
        // 在业务线程中反序列化, 减轻IO线程负担
        return serializer.readObject(bytes, MessageWrapper.class);
    }

    public byte[] doSerializer(ResultWrapper resultWrapper) {
        return serializer.writeObject(resultWrapper);
    }
}
