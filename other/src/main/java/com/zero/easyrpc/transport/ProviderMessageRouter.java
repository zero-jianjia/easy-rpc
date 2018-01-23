
package com.zero.easyrpc.transport;

import com.zero.easyrpc.transport.api.Channel;
import com.zero.easyrpc.rpc.Provider;
import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * service 消息处理
 * <p>
 * <pre>
 * 		1） 多个service的支持
 * 		2） 区分service的方式： group/interface/version
 * </pre>
 */
public class ProviderMessageRouter implements MessageHandler {
    protected Map<String, Provider<?>> providers = new HashMap<>();

    // 所有暴露出去的方法计数
    // 比如：messageRouter 里面涉及2个Service: ServiceA 有5个public method，ServiceB有10个public method，那么就是15
    protected AtomicInteger methodCounter = new AtomicInteger(0);

    public ProviderMessageRouter() {
    }

    public ProviderMessageRouter(Provider<?> provider) {
        addProvider(provider);
    }

    @Override
    public Object handle(Channel channel, Object message) {
        if (channel == null || message == null) {
            throw new RuntimeException("RequestRouter handler(channel, message) params is null");
        }

        if (!(message instanceof Request)) {
            throw new RuntimeException("RequestRouter message type not support: " + message.getClass());
        }

        Request request = (Request) message;

        //目前根据 group/interface/version 来唯一标示一个服务
        //request 中取得serviceKey
        String serviceKey = null;

        Provider<?> provider = providers.get(serviceKey);

        if (provider == null) {
//            MotanServiceException exception =
//                    new MotanServiceException(this.getClass().getSimpleName() + " handler Error: provider not exist serviceKey="
//                            + serviceKey + " " + MotanFrameworkUtil.toString(request));

//            DefaultResponse response = new DefaultResponse();
//            response.setException(exception);
            return null;
        }
        Method method = provider.lookupMethod(request.getMethodName(), request.getParamtersDesc());
        fillParamDesc(request, method);
        processLazyDeserialize(request, method);
        return call(request, provider);
    }

    protected Response call(Request request, Provider<?> provider) {
        try {
            return provider.call(request);
        } catch (Exception e) {
//            DefaultResponse response = new DefaultResponse();
//            response.setException(new MotanBizException("provider call process error", e));
            return null;
        }
    }

    private void processLazyDeserialize(Request request, Method method) {
//        if (method != null && request.getArguments() != null && request.getArguments().length == 1
//                && request.getArguments()[0] instanceof DeserializableObject
//                && request instanceof DefaultRequest) {
//            try {
//                Object[] args = ((DeserializableObject) request.getArguments()[0]).deserializeMulti(method.getParameterTypes());
//                ((DefaultRequest) request).setArguments(args);
//            } catch (IOException e) {
//                throw new MotanFrameworkException("deserialize parameters fail: " + request.toString());
//            }
//        }
    }

    private void fillParamDesc(Request request, Method method) {
//        if (method != null && StringUtils.isBlank(request.getParamtersDesc())
//                && request instanceof DefaultRequest) {
//            DefaultRequest dr = (DefaultRequest) request;
//            dr.setParamtersDesc(ReflectUtil.getMethodParamDesc(method));
//            dr.setMethodName(method.getName());
//        }
    }

    public synchronized void addProvider(Provider<?> provider) {
//        String serviceKey = MotanFrameworkUtil.getServiceKey(provider.getUrl());

        String serviceKey = null;
        if (providers.containsKey(serviceKey)) {
            throw new RuntimeException("provider alread exist: " + serviceKey);
        }

        providers.put(serviceKey, provider);

        // 获取该service暴露的方法数：
        List<Method> methods = com.zero.easyrpc.common.utils.ReflectUtil.getPublicMethod(provider.getInterface());
//        CompressRpcCodec.putMethodSign(provider, methods);// 对所有接口方法生成方法签名。适配方法签名压缩调用方式。

        int publicMethodCount = methods.size();
        methodCounter.addAndGet(publicMethodCount);

    }

    public synchronized void removeProvider(Provider<?> provider) {
        //        String serviceKey = MotanFrameworkUtil.getServiceKey(provider.getUrl());

//        String serviceKey = null;
//
//        providers.remove(serviceKey);
//        List<Method> methods = ReflectUtil.getPublicMethod(provider.getInterface());
//        int publicMethodCount = methods.size();
//        methodCounter.getAndSet(methodCounter.get() - publicMethodCount);

    }

    public int getPublicMethodCount() {
        return methodCounter.get();
    }
}
