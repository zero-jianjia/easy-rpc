package com.zero.rpc;

import com.zero.rpc.model.ServiceWrapper;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Lists;
import org.zero.common.util.Maps;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class DefaultLocalProviderContainer implements LocalProviderContainer {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultLocalProviderContainer.class);

    private final ConcurrentMap<String, ServiceWrapper> serviceProviderMap = Maps.newConcurrentMap();

    @Override
    public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) {
        //uniqueKey: group-interfaceName-version
        serviceProviderMap.put(uniqueKey, serviceWrapper);

        if (logger.isDebugEnabled()) {
            logger.debug("ServiceProvider [{}, {}] is registered.", uniqueKey, serviceWrapper.getServiceProvider());
        }
    }

    @Override
    public ServiceWrapper lookupService(String uniqueKey) {
        return serviceProviderMap.get(uniqueKey);
    }

    @Override
    public ServiceWrapper removeService(String uniqueKey) {
        ServiceWrapper provider = serviceProviderMap.remove(uniqueKey);
        if (provider == null) {
            logger.warn("ServiceProvider [{}] not found.", uniqueKey);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("ServiceProvider [{}, {}] is removed.", uniqueKey, provider.getServiceProvider());
            }
        }
        return provider;
    }

    @Override
    public List<ServiceWrapper> getAllServices() {
        return Lists.newArrayList(serviceProviderMap.values());
    }
}