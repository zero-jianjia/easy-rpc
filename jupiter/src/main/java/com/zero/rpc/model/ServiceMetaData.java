package com.zero.rpc.model;

import com.zero.transport.Directory;

import java.io.Serializable;

import static org.zero.common.util.Preconditions.checkNotNull;

/**
 * Service provider's metadata.
 *
 * 服务的元数据,表征一个服务
 *
 */
public class ServiceMetaData extends Directory implements Serializable {

    private static final long serialVersionUID = -8974194892193662571L;

    private String group;               // 服务组别
    private String serviceProviderName; // 服务名称
    private String version;             // 服务版本号

    public ServiceMetaData() {}

    public ServiceMetaData(String group, String serviceProviderName, String version) {
        this.group = checkNotNull(group, "group");
        this.serviceProviderName = checkNotNull(serviceProviderName, "serviceProviderName");
        this.version = checkNotNull(version, "version");
    }

    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMetaData metadata = (ServiceMetaData) o;

        return group.equals(metadata.group)
                && serviceProviderName.equals(metadata.serviceProviderName)
                && version.equals(metadata.version);
    }

    @Override
    public int hashCode() {
        int result = group.hashCode();
        result = 31 * result + serviceProviderName.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServiceMetaData{" +
                "group='" + group + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
