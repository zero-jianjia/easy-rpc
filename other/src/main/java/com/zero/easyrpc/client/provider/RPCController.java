package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.metrics.ServiceMeterManager;
import com.zero.easyrpc.client.provider.flow.control.ServiceFlowControllerManager;
import com.zero.easyrpc.client.provider.model.ServiceState;
import com.zero.easyrpc.client.provider.model.ServiceWrapper;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.common.transport.body.RequestCustomBody;
import com.zero.easyrpc.common.transport.body.ResponseCustomBody;
import com.zero.easyrpc.common.utils.Pair;
import com.zero.easyrpc.common.utils.Reflects;
import com.zero.easyrpc.common.utils.Status;
import com.zero.easyrpc.common.utils.SystemClock;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 处理consumer rpc请求的核心控制器，并统计处理的次数
 */
public class RPCController {
    private static final Logger logger = LoggerFactory.getLogger(RPCController.class);

    private DefaultProvider defaultProvider;

    public RPCController(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    //处理RPC请求
    public void handlerRPCRequest(Transporter request, Channel channel) {

        String serviceName = null;
        RequestCustomBody body = null;
        int requestSize = 0;

        try {
            byte[] bytes = request.getBytes();
            requestSize = bytes.length;
            request.setBytes(null);

            body = SerializerFactory.serializerImpl().readObject(bytes, RequestCustomBody.class);

            request.setContent(body);

            serviceName = body.getServiceName();

            ServiceMeterManager.incrementRequestSize(serviceName, requestSize);
            ServiceMeterManager.incrementCallTimes(serviceName);

        } catch (Exception e) {
            rejected(Status.BAD_REQUEST, channel, request, serviceName);
            return;
        }

        final Pair<ServiceState, ServiceWrapper> pair = defaultProvider.getRegistryController().getServiceContainer().lookupService(serviceName);
        if (pair == null || pair.getValue() == null) {
            rejected(Status.SERVICE_NOT_FOUND, channel, request, serviceName);
            return;
        }

        if (pair.getValue().isFlowController()) {
            ServiceFlowControllerManager serviceFlowControllerManager = defaultProvider.getRegistryController().getServiceFlowControllerManager();
            if (!serviceFlowControllerManager.isAllow(serviceName)) {
                rejected(Status.APP_FLOW_CONTROL, channel, request, serviceName);
                return;
            }
        }

        process(pair, request, channel, serviceName, body.getTimestamp());
    }


    /**
     * RPC的核心处理逻辑
     */
    private void process(Pair<ServiceState, ServiceWrapper> pair, final Transporter request, Channel channel, final String serviceName, final long beginTime) {

        Object invokeResult = null;

        ServiceState serviceState = pair.getKey();
        ServiceWrapper serviceWrapper = pair.getValue();

        Object targetCallObj = serviceWrapper.getServiceProvider();

        Object[] args = ((RequestCustomBody) request.getContent()).getArgs();

        //判断服务是否已经被设定为自动降级，如果被设置为自动降级且有它自己的mock类的话，则将targetCallObj切换到mock方法上来
        if (serviceState.getDegrade().get() && serviceWrapper.getMockDegradeServiceProvider() != null) {
            targetCallObj = serviceWrapper.getMockDegradeServiceProvider();
        }

        String methodName = serviceWrapper.getMethodName();
        List<Class<?>[]> parameterTypesList = serviceWrapper.getParamters();


        Class<?>[] parameterTypes = Reflects.findMatchingParameterTypes(parameterTypesList, args);
        invokeResult =  Reflects.fastInvoke(targetCallObj, methodName, parameterTypes, args);

        ResponseCustomBody.ResultWrapper result = new ResponseCustomBody.ResultWrapper();
        result.setResult(invokeResult);
        ResponseCustomBody body = new ResponseCustomBody(Status.OK.value(), result);

        final Transporter response = Transporter.createResponseTransporter(Protocol.RPC_RESPONSE, body, request.getRequestId());

        channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {

                long elapsed = SystemClock.millisClock().now() - beginTime;

                if (future.isSuccess()) {
                    ServiceMeterManager.incrementTotalTime(serviceName, elapsed);
                } else {
                    logger.info("request {} get failed response {}", request, response);
                }
            }
        });
    }

    private void rejected(Status status, Channel channel, final Transporter request, String serviceName) {

        if (null != serviceName) {
            ServiceMeterManager.incrementFailTimes(serviceName);
        }
        ResponseCustomBody.ResultWrapper result = new ResponseCustomBody.ResultWrapper();
        switch (status) {
            case BAD_REQUEST:
                result.setError("bad request");
            case SERVICE_NOT_FOUND:
                result.setError(((RequestCustomBody) request.getContent()).getServiceName() + " no service found");
                break;
            case APP_FLOW_CONTROL:
            case PROVIDER_FLOW_CONTROL:
                result.setError("over unit time call limit");
                break;
            default:
                logger.warn("Unexpected status.", status.description());
                return;
        }
        logger.warn("Service rejected: {}.", result.getError());

        ResponseCustomBody responseCustomBody = new ResponseCustomBody(status.value(), result);
        final Transporter response = Transporter.createResponseTransporter(Protocol.RPC_RESPONSE, responseCustomBody,
                request.getRequestId());

        channel.writeAndFlush(response).addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("request error {} get success response {}", request, response);
                } else {
                    logger.info("request error {} get failed response {}", request, response);
                }
            }
        });
    }

}
