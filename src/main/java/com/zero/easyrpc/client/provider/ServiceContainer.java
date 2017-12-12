package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.client.provider.model.ServiceState;
import com.zero.easyrpc.client.provider.model.ServiceWrapper;
import com.zero.easyrpc.common.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 存放Provider提供的服务
 * Created by jianjia1 on 17/12/04.
 */
public class ServiceContainer {

    private final Map<String, Pair<ServiceState, ServiceWrapper>> serviceProviders = new ConcurrentHashMap<>();

    /**
     * 将服务放置在服务容器中，用来进行统一的管理
     * @param serviceName 该服务的名称
     * @param serviceName 该服务的包装编织类
     */
    public void registerService(String serviceName, ServiceWrapper serviceWrapper) {
        Pair<ServiceState, ServiceWrapper> pair = new Pair<>();
        pair.setKey(new ServiceState());
        pair.setValue(serviceWrapper);
        serviceProviders.put(serviceName, pair);
    }

    /**
     * 根据服务的名称来获取对应的服务编织类
     * @param serviceName 服务名
     */
    public Pair<ServiceState, ServiceWrapper> lookupService(String serviceName) {
        return serviceProviders.get(serviceName);
    }

    /**
     * 获取到所有需要自动降级的服务
     */
    public List<Pair<String, ServiceState>> getNeedAutoDegradeService() {

        Map<String, Pair<ServiceState, ServiceWrapper>> providers = this.serviceProviders;
        List<Pair<String, ServiceState>> list = new ArrayList<>();

        for (Map.Entry<String, Pair<ServiceState, ServiceWrapper>> servicePair : providers.entrySet()) {
            Pair<ServiceState, ServiceWrapper> pair = servicePair.getValue();

            //如果已经设置成自动降级的时候
            if (pair != null && pair.getKey().getIsAutoDegrade().get()) {
                Pair<String, ServiceState> targetPair = new Pair<>();
                targetPair.setKey(servicePair.getKey());
                targetPair.setValue(pair.getKey());
                list.add(targetPair);
            }
        }
        return list;
    }


}
