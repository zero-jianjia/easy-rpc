package com.zero.easyrpc.rpc.protocol;


import com.zero.easyrpc.transport.api.Client;
import com.zero.easyrpc.rpc.*;
import com.zero.easyrpc.transport.EndpointFactory;

/**
 * 服务消费者
 * 1.Class  接口类
 * 2.Client
 *
 */
public class DefaultRpcReferer<T> extends AbstractReferer<T> {
    protected Client client;
    protected EndpointFactory endpointFactory;

    public DefaultRpcReferer(Class<T> clz, URL url, URL serviceUrl) {
        super(clz, url, serviceUrl);

        endpointFactory =null;

        client = endpointFactory.createClient(url);
    }

    @Override
    protected Response doCall(Request request) {
//        try {
            // 为了能够实现跨group请求，需要使用server端的group。
//            request.setAttachment(URLParamType.group.getName(), serviceUrl.getGroup());
//            return client.request(request);
            return null;
//        } catch (TransportException exception) {
//            throw new MotanServiceException("DefaultRpcReferer call Error: url=" + url.getUri(), exception);
//            exception.printStackTrace();
//        }
    }

    @Override
    protected void decrActiveCount(Request request, Response response) {
        if (response == null || !(response instanceof Future)) {
            activeRefererCount.decrementAndGet();
            return;
        }

        Future future = (Future) response;

        future.addListener(new FutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                activeRefererCount.decrementAndGet();
            }
        });
    }

    @Override
    protected boolean doInit() {
        boolean result = client.open();

        return result;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        endpointFactory.safeReleaseResource(client, url);
    }

}
