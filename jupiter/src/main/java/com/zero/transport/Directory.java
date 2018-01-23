
package com.zero.transport;

import org.zero.common.util.StringBuilderHelper;

/**
 * 服务目录: <服务组别, 服务名称, 服务版本号>
 */
public abstract class Directory {

    private String directoryCache;

    public abstract String getGroup();

    public abstract String getServiceProviderName(); //接口类类名

    public abstract String getVersion();

    public String directory() {
        if (directoryCache != null) {
            return directoryCache;
        }

        StringBuilder buf = StringBuilderHelper.get();
        buf.append(getGroup())
                .append('-')
                .append(getServiceProviderName())
                .append('-')
                .append(getVersion());

        directoryCache = buf.toString();

        return directoryCache;
    }

    public void clear() {
        directoryCache = null;
    }
}
