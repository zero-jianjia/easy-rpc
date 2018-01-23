package com.zero.rpc.model;

import com.zero.rpc.Request;
import org.zero.common.util.Lists;
import org.zero.common.util.Maps;
import org.zero.common.util.Pair;
import org.zero.common.util.Strings;
import com.zero.rpc.ServiceProvider;
import com.zero.rpc.ServiceProviderImpl;
import com.zero.rpc.provider.flow.control.FlowController;
import com.zero.rpc.provider.ProviderInterceptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.zero.common.util.Preconditions.checkArgument;
import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * 对一个具体的服务初步包装
 * 但是，在RPCServer中保存的实际上是ServiceWrapper
 * ServiceWrapper包含RPCServer的全局interceptors，而ServiceRegistry只是该服务级别的
 */
public class ServiceRegistry {

    private Object serviceProvider;                     // 服务对象
    private ProviderInterceptor[] interceptors;         // 拦截器
    private Class<?> interfaceClass;                    // 接口类型
    private String group;                               // 服务组别
    private String providerName;                        // 服务名称
    private String version;                             // 服务版本号, 通常在接口不兼容时版本号才需要升级
    private int weight;                                 // 权重
    private Executor executor;                          // 该服务私有的线程池
    private FlowController<Request> flowController;    // 该服务私有的流量控制器
    private Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions;

    public static class Builder {
        private Object serviceProvider;                     // 服务对象
        private ProviderInterceptor[] interceptors;         // 拦截器
        private Class<?> interfaceClass;                    // 接口类型
        private String group;                               // 服务组别
        private String providerName;                        // 服务名称
        private String version;                             // 服务版本号, 通常在接口不兼容时版本号才需要升级
        private int weight;                                 // 权重
        private Executor executor;                          // 该服务私有的线程池
        private FlowController<Request> flowController;    // 该服务私有的流量控制器
        private Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions;

        public Builder provider(Object serviceProvider, ProviderInterceptor... interceptors) {
            this.serviceProvider = serviceProvider;
            this.interceptors = interceptors;
            return this;
        }

        public Builder interfaceClass(Class<?> interfaceClass) {
            this.interfaceClass = interfaceClass;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder flowController(FlowController<Request> flowController) {
            this.flowController = flowController;
            return this;
        }

        public ServiceRegistry build() {
            return new ServiceRegistry(this);
        }

    }

    public ServiceRegistry(Builder build) {
        serviceProvider = build.serviceProvider;
        interceptors = build.interceptors;
        interfaceClass = build.interfaceClass;
        group = build.group;
        providerName = build.providerName;
        version = build.version;
        weight = build.weight;
        executor = build.executor;
        flowController = build.flowController;

        build = null;

        checkNotNull(serviceProvider, "serviceProvider");
        Class<?> providerClass = serviceProvider.getClass();


        ServiceProviderImpl implAnnotation = null;
        ServiceProvider ifAnnotation = null;
        for (Class<?> cls = providerClass; cls != Object.class; cls = cls.getSuperclass()) {
            if (implAnnotation == null) {
                implAnnotation = cls.getAnnotation(ServiceProviderImpl.class);
            }

            Class<?>[] interfaces = cls.getInterfaces();
            if (interfaces != null) {
                for (Class<?> i : interfaces) {
                    ifAnnotation = i.getAnnotation(ServiceProvider.class);
                    if (ifAnnotation == null) {
                        continue;
                    }

                    checkArgument(interfaceClass == null,
                            i.getName() + " has a @ServiceProvider annotation, can't set [interfaceClass] again");

                    interfaceClass = i;
                    break;
                }
            }

            if (implAnnotation != null && ifAnnotation != null) {
                break;
            }
        }

        if (ifAnnotation != null) {
            checkArgument(group == null,
                    interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [group] again");
            checkArgument(providerName == null,
                    interfaceClass.getName() + " has a @ServiceProvider annotation, can't set [providerName] again");

            group = ifAnnotation.group();
            String name = ifAnnotation.name();
            providerName = Strings.isNotBlank(name) ? name : interfaceClass.getName();
        }

        if (implAnnotation != null) {
            checkArgument(version == null,
                    providerClass.getName() + " has a @ServiceProviderImpl annotation, can't set [version] again");

            version = implAnnotation.version();
        }

        checkNotNull(interfaceClass, "interfaceClass");
        checkArgument(Strings.isNotBlank(group), "group");
        checkArgument(Strings.isNotBlank(providerName), "providerName");
        checkArgument(Strings.isNotBlank(version), "version");

        // method's extensions
        //
        // key:     method name
        // value:   pair.first:  方法参数类型(用于根据JLS规则实现方法调用的静态分派)
        //          pair.second: 方法显式声明抛出的异常类型
        extensions = Maps.newHashMap();
        for (Method method : interfaceClass.getMethods()) {
            String methodName = method.getName();
            List<Pair<Class<?>[], Class<?>[]>> list = extensions.get(methodName);
            if (list == null) {
                list = Lists.newArrayList();
                extensions.put(methodName, list);
            }
            list.add(Pair.of(method.getParameterTypes(), method.getExceptionTypes()));
        }
    }


    public Object getServiceProvider() {
        return serviceProvider;
    }

    public ProviderInterceptor[] getInterceptors() {
        return interceptors;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public String getGroup() {
        return group;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getVersion() {
        return version;
    }

    public int getWeight() {
        return weight;
    }

    public Executor getExecutor() {
        return executor;
    }

    public FlowController<Request> getFlowController() {
        return flowController;
    }


    public Map<String, List<Pair<Class<?>[], Class<?>[]>>> getExtensions() {
        return extensions;
    }
}
