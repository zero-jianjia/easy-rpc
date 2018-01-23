package com.zero.easyrpc.cluster.ha;

import com.zero.easyrpc.cluster.LoadBalance;
import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.Request;
import com.zero.easyrpc.rpc.Response;
import com.zero.easyrpc.rpc.URL;

import java.util.ArrayList;
import java.util.List;

/**
 * Failover ha strategy.
 */
public class FailoverHaStrategy<T> extends AbstractHaStrategy<T> {

    protected ThreadLocal<List<Referer<T>>> referersHolder = new ThreadLocal<List<Referer<T>>>() {
        @Override
        protected List<Referer<T>> initialValue() {
            return new ArrayList<Referer<T>>();
        }
    };

    @Override
    public Response call(Request request, LoadBalance<T> loadBalance) {

        List<Referer<T>> referers = selectReferers(request, loadBalance);
        if (referers.isEmpty()) {
//            throw new MotanServiceException(String.format("FailoverHaStrategy No referers for request:%s, loadbalance:%s", request,
//                    loadBalance));
        }
        URL refUrl = referers.get(0).getUrl();
        // 先使用method的配置
        int tryCount = 10;
//                refUrl.getMethodParameter(request.getMethodName(), request.getParamtersDesc(), URLParamType.retries.getName(),
//                        URLParamType.retries.getIntValue());
        // 如果有问题，则设置为不重试
        if (tryCount < 0) {
            tryCount = 0;
        }

        for (int i = 0; i <= tryCount; i++) {
            Referer<T> refer = referers.get(i % referers.size());
            try {
                request.setRetries(i);
                return refer.call(request);
            } catch (RuntimeException e) {
                // 对于业务异常，直接抛出
//                if (ExceptionUtil.isBizException(e)) {
//                    throw e;
//                } else
                if (i >= tryCount) {
                    throw e;
                }

//                LoggerUtil.warn(String.format("FailoverHaStrategy Call false for request:%s error=%s", request, e.getMessage()));
            }
        }

        throw new RuntimeException("FailoverHaStrategy.call should not come here!");
    }

    protected List<Referer<T>> selectReferers(Request request, LoadBalance<T> loadBalance) {
        List<Referer<T>> referers = referersHolder.get();
        referers.clear();
        loadBalance.selectToHolder(request, referers);
        return referers;
    }

}
