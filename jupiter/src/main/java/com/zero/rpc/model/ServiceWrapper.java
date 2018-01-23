package com.zero.rpc.model;


import org.zero.common.util.JConstants;
import org.zero.common.util.Pair;
import com.zero.rpc.Request;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.provider.ProviderInterceptor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * Wrapper provider object and service metadata.
 * <p>
 * 服务元数据 & 服务对象
 */
public class ServiceWrapper implements Serializable {
    private static final long serialVersionUID = 6690575889849847348L;

    // 服务元数据
    private final ServiceMetaData metadata;
    // 服务对象
    private final Object serviceProvider;

    // 服务拦截器
    private final ProviderInterceptor[] interceptors;
    // provider私有流量控制器
    private FlowController<Request> flowController;

    // key:     method name
    // value:   pair.first:  方法参数类型(用于根据JLS规则实现方法调用的静态分派)
    //          pair.second: 方法显式声明抛出的异常类型
    private final Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions;

    // 权重 hashCode() 与 equals() 不把weight计算在内
    private int weight = JConstants.DEFAULT_WEIGHT;
    // provider私有线程池
    private Executor executor;


    public ServiceWrapper(String group,
            String providerName,
            String version,
            Object serviceProvider,
            ProviderInterceptor[] interceptors,
            Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions) {

        metadata = new ServiceMetaData(group, providerName, version);

        this.interceptors = interceptors;
        this.extensions = checkNotNull(extensions, "extensions");
        this.serviceProvider = checkNotNull(serviceProvider, "serviceProvider");
    }

    public ServiceMetaData getMetadata() {
        return metadata;
    }

    public Object getServiceProvider() {
        return serviceProvider;
    }

    public ProviderInterceptor[] getInterceptors() {
        return interceptors;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public FlowController<Request> getFlowController() {
        return flowController;
    }

    public void setFlowController(FlowController<Request> flowController) {
        this.flowController = flowController;
    }

    public List<Pair<Class<?>[], Class<?>[]>> getMethodExtension(String methodName) {
        return extensions.get(methodName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceWrapper wrapper = (ServiceWrapper) o;

        return metadata.equals(wrapper.metadata);
    }

    @Override
    public int hashCode() {
        return metadata.hashCode();
    }

    @Override
    public String toString() {
        return "ServiceWrapper{" +
                "metadata=" + metadata +
                ", serviceProvider=" + serviceProvider +
                ", interceptors=" + Arrays.toString(interceptors) +
                ", extensions=" + extensions +
                ", weight=" + weight +
                ", executor=" + executor +
                ", flowController=" + flowController +
                '}';
    }
}
