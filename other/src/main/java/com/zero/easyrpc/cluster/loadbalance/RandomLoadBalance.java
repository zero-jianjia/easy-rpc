package com.zero.easyrpc.cluster.loadbalance;


import com.zero.easyrpc.rpc.Referer;
import com.zero.easyrpc.rpc.Request;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * random load balance.
 *
 */
public class RandomLoadBalance<T> extends AbstractLoadBalance<T> {

    @Override
    protected Referer<T> doSelect(Request request) {
        List<Referer<T>> referers = getReferers();

        int idx = (int) (ThreadLocalRandom.current().nextDouble() * referers.size());
        for (int i = 0; i < referers.size(); i++) {
            Referer<T> ref = referers.get((i + idx) % referers.size());
            if (ref.isAvailable()) {
                return ref;
            }
        }
        return null;
    }

    @Override
    protected void doSelectToHolder(Request request, List<Referer<T>> refersHolder) {
        List<Referer<T>> referers = getReferers();

        int idx = (int) (ThreadLocalRandom.current().nextDouble() * referers.size());
        for (int i = 0; i < referers.size(); i++) {
            Referer<T> referer = referers.get((i + idx) % referers.size());
            if (referer.isAvailable()) {
                refersHolder.add(referer);
            }
        }
    }
}
