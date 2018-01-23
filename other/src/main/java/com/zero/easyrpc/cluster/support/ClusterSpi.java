
package com.zero.easyrpc.cluster.support;


import com.zero.easyrpc.cluster.Cluster;
import com.zero.easyrpc.cluster.HAStrategy;
import com.zero.easyrpc.cluster.LoadBalance;
import com.zero.easyrpc.rpc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cluster spi.
 * @author fishermen
 * @version V1.0 created at: 2013-5-21
 */
public class ClusterSpi<T> implements Cluster<T> {

    private HAStrategy<T> haStrategy;

    private LoadBalance<T> loadBalance;

    private List<Referer<T>> referers;

    private AtomicBoolean available = new AtomicBoolean(false);

    private URL url;

    @Override
    public void init() {
        onRefresh(referers);
        available.set(true);
    }

    @Override
    public Class<T> getInterface() {
        if (referers == null || referers.isEmpty()) {
            return null;
        }

        return referers.get(0).getInterface();
    }

    @Override
    public Response call(Request request) {
        if (available.get()) {
            try {
                return haStrategy.call(request, loadBalance);
            } catch (Exception e) {
                return callFalse(request, e);
            }
        }
        return callFalse(request, new RuntimeException());
    }

    @Override
    public String desc() {
        return toString();
    }

    @Override
    public void destroy() {
        available.set(false);
        for (Referer<T> referer : this.referers) {
            referer.destroy();
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public String toString() {
        return "cluster: {" + "ha=" + haStrategy + ",loadbalance=" + loadBalance +
                "referers=" + referers + "}";

    }

    @Override
    public synchronized void onRefresh(List<Referer<T>> referers) {
        if (referers.isEmpty()) {
            return;
        }

        loadBalance.onRefresh(referers);
        List<Referer<T>> oldReferers = this.referers;
        this.referers = referers;
        haStrategy.setUrl(getUrl());

        if (oldReferers == null || oldReferers.isEmpty()) {
            return;
        }

        List<Referer<T>> delayDestroyReferers = new ArrayList<>();

        for (Referer<T> referer : oldReferers) {
            if (referers.contains(referer)) {
                continue;
            }

            delayDestroyReferers.add(referer);
        }

        if (!delayDestroyReferers.isEmpty()) {
            RefererSupports.delayDestroy(delayDestroyReferers);
        }
    }

    public AtomicBoolean getAvailable() {
        return available;
    }

    public void setAvailable(AtomicBoolean available) {
        this.available = available;
    }

    public HAStrategy<T> getHaStrategy() {
        return haStrategy;
    }

    @Override
    public void setHaStrategy(HAStrategy<T> haStrategy) {
        this.haStrategy = haStrategy;
    }

    @Override
    public LoadBalance<T> getLoadBalance() {
        return loadBalance;
    }

    @Override
    public void setLoadBalance(LoadBalance<T> loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public List<Referer<T>> getReferers() {
        return referers;
    }

    protected Response callFalse(Request request, Exception cause) {

        // biz exception 无论如何都要抛出去
//        if (ExceptionUtil.isBizException(cause)) {
//            throw (RuntimeException) cause;
//        }

        // 其他异常根据配置决定是否抛，如果抛异常，需要统一为MotanException
//        if (Boolean.parseBoolean(getUrl().getParameter(URLParamType.throwException.getName(), URLParamType.throwException.getValue()))) {
//            if (cause instanceof MotanAbstractException) {
//                throw (MotanAbstractException) cause;
//            } else {
//                MotanServiceException motanException =
//                        new MotanServiceException(String.format("ClusterSpi Call false for request: %s", request), cause);
//                throw motanException;
//            }
//        }

        return buildErrorResponse(request, cause);
    }

    private Response buildErrorResponse(Request request, Exception motanException) {
        DefaultResponse rs = new DefaultResponse();
        rs.setException(motanException);
        rs.setRequestId(request.getRequestId());
        return rs;
    }

}
