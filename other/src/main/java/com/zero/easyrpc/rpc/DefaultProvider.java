package com.zero.easyrpc.rpc;

import java.lang.reflect.Method;

/**
 * 具体的方法调用的提供者
 * 1. 必须拥有实现类的实例
 */
public class DefaultProvider<T> extends AbstractProvider<T> {
    protected T proxyImpl;

    public DefaultProvider(T proxyImpl, URL url, Class<T> clz) {
        super(url, clz);
        this.proxyImpl = proxyImpl;
    }

    @Override
    public T getImpl() {
        return proxyImpl;
    }

    @Override
    public Response invoke(Request request) {
        DefaultResponse response = new DefaultResponse();

        Method method = lookupMethod(request.getMethodName(), request.getParamtersDesc());

        if (method == null) {
            Exception exception = new Exception("Service method not exist: " + request.getInterfaceName() + "." + request.getMethodName()
                    + "(" + request.getParamtersDesc() + ")");

            response.setException(exception);
            return response;
        }

        try {
            Object value = method.invoke(proxyImpl, request.getArguments());
            response.setValue(value);
        } catch (Exception e) {
            if (e.getCause() != null) {
                response.setException(new Exception("provider call process error", e.getCause()));
            } else {
                response.setException(new Exception("provider call process error", e));
            }
            //服务发生错误时，显示详细日志
        } catch (Throwable t) {
            // 如果服务发生Error，将Error转化为Exception，防止拖垮调用方
            if (t.getCause() != null) {
                response.setException(new Exception("provider has encountered a fatal error!", t.getCause()));
            } else {
                response.setException(new Exception("provider has encountered a fatal error!", t));
            }
            //对于Throwable,也记录日志
        }

        if (response.getException() != null) {
            //是否传输业务异常栈
        }
        // 传递rpc版本和attachment信息方便不同rpc版本的codec使用。
        response.setRpcProtocolVersion(request.getRpcProtocolVersion());
        response.setAttachments(request.getAttachments());
        return response;
    }
}
