package com.zero.easyrpc.common.spi;

import java.util.ServiceLoader;

/**
 * Created by jianjia1 on 17/12/04.
 */
public class BaseServiceLoader {
    public static <S> S load(Class<S> serviceClass) {
        return ServiceLoader.load(serviceClass).iterator().next();
    }
}
