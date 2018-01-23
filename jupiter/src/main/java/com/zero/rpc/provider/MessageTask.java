package com.zero.rpc.provider;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.zero.rpc.Request;
import com.zero.rpc.metric.Metrics;
import com.zero.rpc.model.MessageWrapper;
import com.zero.rpc.model.ResultWrapper;
import com.zero.rpc.model.ServiceWrapper;
import com.zero.rpc.provider.flow.control.ControlResult;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.tracing.TraceId;
import com.zero.rpc.tracing.TracingUtil;
import com.zero.transport.Status;
import com.zero.transport.api.ResponseBytes;
import com.zero.transport.api.channel.Channel;
import com.zero.transport.api.channel.FutureListener;
import io.netty.util.Signal;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.concurrent.RejectedRunnable;
import org.zero.common.util.Pair;
import org.zero.common.util.Reflects;
import org.zero.common.util.SystemClock;
import org.zero.common.util.SystemPropertyUtil;
import org.zero.common.util.internal.UnsafeIntegerFieldUpdater;
import org.zero.common.util.internal.UnsafeUpdater;
import org.zero.rpc.exception.*;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.zero.common.util.Preconditions.checkNotNull;
import static org.zero.common.util.StackTraceUtil.stackTrace;

public class MessageTask implements RejectedRunnable {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(MessageTask.class);

    private static final boolean METRIC_NEEDED = SystemPropertyUtil.getBoolean("jupiter.metric.needed", false);
    private static final Signal INVOKE_ERROR = Signal.valueOf(MessageTask.class, "INVOKE_ERROR");

    private static final UnsafeIntegerFieldUpdater<TraceId> traceNodeUpdater =
            UnsafeUpdater.newIntegerFieldUpdater(TraceId.class, "node");

    private final DefaultProviderProcessor processor;
    private final Channel channel;
    private Request request;

    public MessageTask(DefaultProviderProcessor processor, Channel channel, Request request) {
        this.processor = processor;
        this.channel = channel;
        this.request = request;
    }

    @Override
    public void run() {
        // stack copy
        final DefaultProviderProcessor providerProcessor = processor;
        final Request _request = request;

        // 全局流量控制
        ControlResult ctrl = providerProcessor.flowControl(_request);
        if (!ctrl.isAllowed()) {
            rejected(Status.APP_FLOW_CONTROL, new JupiterFlowControlException(String.valueOf(ctrl)));
            return;
        }

        MessageWrapper message;
        try {
            message = providerProcessor.doSerializer(_request.requestBytes());
            _request.message(message);
        } catch (Throwable t) {
            rejected(Status.BAD_REQUEST, new JupiterBadRequestException(t.getMessage()));
            return;
        }

        // 查找服务
        final ServiceWrapper service = providerProcessor.lookupService(message.getMetadata());
        if (service == null) {
            rejected(Status.SERVICE_NOT_FOUND, new JupiterServiceNotFoundException(String.valueOf(message)));
            return;
        }

        // provider私有流量控制
        FlowController<Request> childController = service.getFlowController();
        if (childController != null) {
            ctrl = childController.flowControl(_request);
            if (!ctrl.isAllowed()) {
                rejected(Status.PROVIDER_FLOW_CONTROL, new JupiterFlowControlException(String.valueOf(ctrl)));
                return;
            }
        }

        // processing
        Executor childExecutor = service.getExecutor();
        if (childExecutor == null) {
            process(service);
        } else {
            // provider私有线程池执行
            childExecutor.execute(new Runnable() {

                @Override
                public void run() {
                    process(service);
                }
            });
        }
    }

    @Override
    public void rejected() {
        rejected(Status.SERVER_BUSY, new JupiterServerBusyException(String.valueOf(request)));
    }

    private void rejected(Status status, JupiterRemoteException cause) {
        if (METRIC_NEEDED) {
            MetricsHolder.rejectionMeter.mark();
        }

        // 当服务拒绝方法被调用时一般分以下几种情况:
        //  1. 非法请求, close当前连接;
        //  2. 服务端处理能力出现瓶颈, close当前连接, jupiter客户端会自动重连, 在加权负载均衡的情况下权重是一点一点升上来的.
        processor.handleRejected(channel, request, status, cause);
    }

    private void process(ServiceWrapper service) {
        // stack copy
        final Request _request = request;

        Context invokeCtx = new Context(service);

        if (TracingUtil.isTracingNeeded()) {
            setCurrentTraceId(_request.message().getTraceId());
        }

        try {
            Object invokeResult = null;

            // 拦截器
            ProviderInterceptor[] interceptors = service.getInterceptors();

            if (interceptors == null || interceptors.length == 0) {
                invokeResult = MessageTask.invoke(_request.message(), invokeCtx);
                invokeCtx.setResult(invokeResult);
            } else {
                TraceId traceId = TracingUtil.getCurrent();
                Object provider = service.getServiceProvider();

                MessageWrapper msg = request.message();
                String methodName = msg.getMethodName();
                Object[] args = msg.getArgs();

                handleBeforeInvoke(interceptors, traceId, provider, methodName, args);
                try {
                    invokeResult = MessageTask.invoke(_request.message(), invokeCtx);
                    invokeCtx.setResult(invokeResult);
                } finally {
                    Throwable cause = invokeCtx.getCause();
                    handleAfterInvoke(interceptors, traceId, provider, methodName, args, invokeResult, cause);
                }
            }


            ResultWrapper result = new ResultWrapper();
            result.setResult(invokeResult);

            byte[] bytes = processor.doSerializer(result);

            ResponseBytes response = new ResponseBytes(_request.invokeId());
            response.status(Status.OK.value());
            response.setBytes(bytes);

            handleWriteResponse(response);
        } catch (Throwable t) {
            if (INVOKE_ERROR == t) {
                // handle biz exception
                handleException(invokeCtx.getExpectCauseTypes(), invokeCtx.getCause());
            } else {
                processor.handleException(channel, _request, Status.SERVER_ERROR, t);
            }
        } finally {
            if (TracingUtil.isTracingNeeded()) {
                TracingUtil.clearCurrent();
            }
        }
    }

    private void handleWriteResponse(ResponseBytes response) {
        channel.write(response, new FutureListener<Channel>() {
            @Override
            public void onSuccess(Channel channel) throws Exception {
                if (METRIC_NEEDED) {
                    long duration = SystemClock.millisClock().now() - request.timestamp();
                    MetricsHolder.processingTimer.update(duration, TimeUnit.MILLISECONDS);
                }
            }

            @Override
            public void onFailure(Channel channel, Throwable cause) throws Exception {
                long duration = SystemClock.millisClock().now() - request.timestamp();
                logger.error("Response sent failed, trace: {}, duration: {} millis, channel: {}, cause: {}.",
                        request.getTraceId(), duration, channel, cause);
            }
        });
    }

    private void handleException(Class<?>[] exceptionTypes, Throwable failCause) {
        if (exceptionTypes != null && exceptionTypes.length > 0) {
            Class<?> failType = failCause.getClass();
            for (Class<?> eType : exceptionTypes) {
                // 如果抛出声明异常的子类, 客户端可能会因为不存在子类类型而无法序列化, 会在客户端抛出无法反序列化异常
                if (eType.isAssignableFrom(failType)) {
                    // 预期内的异常
                    processor.handleException(channel, request, Status.SERVICE_EXPECTED_ERROR, failCause);
                    return;
                }
            }
        }

        // 预期外的异常
        processor.handleException(channel, request, Status.SERVICE_UNEXPECTED_ERROR, failCause);
    }

    private static Object invoke(MessageWrapper msg, Context invokeCtx) throws Signal {
        ServiceWrapper service = invokeCtx.getService();
        Object provider = service.getServiceProvider();
        String methodName = msg.getMethodName();
        Object[] args = msg.getArgs();

        Timer.Context timerCtx = null;
        if (METRIC_NEEDED) {
            timerCtx = Metrics.timer(msg.getOperationName()).time();
        }

        Class<?>[] expectCauseTypes = null;
        try {
            List<Pair<Class<?>[], Class<?>[]>> methodExtension = service.getMethodExtension(methodName);
            if (methodExtension == null) {
                throw new NoSuchMethodException(methodName);
            }

            // 根据JLS方法调用的静态分派规则查找最匹配的方法parameterTypes
            Pair<Class<?>[], Class<?>[]> bestMatch = Reflects.findMatchingParameterTypesExt(methodExtension, args);
            Class<?>[] parameterTypes = bestMatch.getFirst();
            expectCauseTypes = bestMatch.getSecond();

            return Reflects.fastInvoke(provider, methodName, parameterTypes, args);
        } catch (Throwable t) {
            invokeCtx.setCauseAndExpectTypes(t, expectCauseTypes);
            throw INVOKE_ERROR;
        } finally {
            if (METRIC_NEEDED) {
                timerCtx.stop();
            }
        }
    }

    @SuppressWarnings("all")
    private static void handleBeforeInvoke(ProviderInterceptor[] interceptors,
            TraceId traceId,
            Object provider,
            String methodName,
            Object[] args) {

        for (int i = 0; i < interceptors.length; i++) {
            try {
                interceptors[i].beforeInvoke(traceId, provider, methodName, args);
            } catch (Throwable t) {
                logger.error("Interceptor[{}#beforeInvoke]: {}.", Reflects.simpleClassName(interceptors[i]), stackTrace(t));
            }
        }
    }

    @SuppressWarnings("all")
    private static void handleAfterInvoke(ProviderInterceptor[] interceptors,
            TraceId traceId,
            Object provider,
            String methodName,
            Object[] args,
            Object invokeResult,
            Throwable failCause) {

        for (int i = interceptors.length - 1; i >= 0; i--) {
            try {
                interceptors[i].afterInvoke(traceId, provider, methodName, args, invokeResult, failCause);
            } catch (Throwable t) {
                logger.error("Interceptor[{}#afterInvoke]: {}.", Reflects.simpleClassName(interceptors[i]), stackTrace(t));
            }
        }
    }

    private static void setCurrentTraceId(TraceId traceId) {
        if (traceId != null && traceId != TraceId.NULL_TRACE_ID) {
            assert traceNodeUpdater != null;
            traceNodeUpdater.set(traceId, traceId.getNode() + 1);
        }
        TracingUtil.setCurrent(traceId);
    }

    public static class Context {

        private final ServiceWrapper service;

        private Object result;                  // 服务调用结果
        private Throwable cause;                // 业务异常
        private Class<?>[] expectCauseTypes;    // 预期内的异常类型

        public Context(ServiceWrapper service) {
            this.service = checkNotNull(service, "service");
        }

        public ServiceWrapper getService() {
            return service;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Throwable getCause() {
            return cause;
        }

        public Class<?>[] getExpectCauseTypes() {
            return expectCauseTypes;
        }

        public void setCauseAndExpectTypes(Throwable cause, Class<?>[] expectCauseTypes) {
            this.cause = cause;
            this.expectCauseTypes = expectCauseTypes;
        }

    }


    // - Metrics -------------------------------------------------------------------------------------------------------
    static class MetricsHolder {
        // 请求处理耗时统计(从request被解码开始, 到response数据被刷到OS内核缓冲区为止)
        static final Timer processingTimer = Metrics.timer("processing");
        // 请求被拒绝次数统计
        static final Meter rejectionMeter = Metrics.meter("rejection");
    }
}