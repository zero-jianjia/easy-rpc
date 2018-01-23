package com.zero.rpc.model;

import org.zero.common.util.Maps;
import com.zero.rpc.tracing.TraceId;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Request data wrapper.
 *
 * 请求消息包装.
 *
 */
public class MessageWrapper implements Serializable {

    private static final long serialVersionUID = 1009813828866652852L;

    private String appName;                 // 应用名称
    private final ServiceMetaData metadata; // 目标服务元数据
    private String methodName;              // 目标方法名称
    private Object[] args;                  // 目标方法参数
    private TraceId traceId;                // 链路追踪ID(全局唯一)
    private Map<String, String> attachments;

    public MessageWrapper(ServiceMetaData metadata) {
        this.metadata = metadata;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public ServiceMetaData getMetadata() {
        return metadata;
    }

    public String getGroup() {
        return metadata.getGroup();
    }

    public String getServiceProviderName() {
        return metadata.getServiceProviderName();
    }

    public String getVersion() {
        return metadata.getVersion();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public void setTraceId(TraceId traceId) {
        this.traceId = traceId;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void putAttachment(String key, String value) {
        if (attachments == null) {
            attachments = Maps.newHashMap();
        }
        attachments.put(key, value);
    }

    public String getOperationName() {
        return metadata.directory() + "." + methodName;
    }

    @Override
    public String toString() {
        return "MessageWrapper{" +
                "appName='" + appName + '\'' +
                ", metadata=" + metadata +
                ", methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", traceId=" + traceId +
                ", attachments=" + attachments +
                '}';
    }
}
