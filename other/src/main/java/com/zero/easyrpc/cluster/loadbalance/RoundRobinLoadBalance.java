package com.zero.easyrpc.cluster.loadbalance;


import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.Request;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Round robin loadbalance.
 * 
 */
public class RoundRobinLoadBalance<T> extends AbstractLoadBalance<T> {

    private AtomicInteger idx = new AtomicInteger(0);

    @Override
    protected Referer<T> doSelect(Request request) {
        List<Referer<T>> referers = getReferers();

        int index = getNextPositive();
        for (int i = 0; i < referers.size(); i++) {
            Referer<T> ref = referers.get((i + index) % referers.size());
            if (ref.isAvailable()) {
                return ref;
            }
        }
        return null;
    }

    @Override
    protected void doSelectToHolder(Request request, List<Referer<T>> refersHolder) {
        List<Referer<T>> referers = getReferers();

        int index = getNextPositive();
        for (int i = 0, count = 0; i < referers.size() && count < MAX_REFERER_COUNT; i++) {
            Referer<T> referer = referers.get((i + index) % referers.size());
            if (referer.isAvailable()) {
                refersHolder.add(referer);
                count++;
            }
        }
    }

    // get positive int
    private int getNextPositive() {
        return 0x7fffffff & idx.incrementAndGet();
    }
}
