
package com.zero.easyrpc.rpc;

/**
 * 协议层

 * protocol
 */
public interface Protocol {
    /**
     * 暴露服务
     * 暴露过程代理给具体协议
     */
    <T> Exporter<T> export(Provider<T> provider, URL url);

    /**
     * 引用服务
     * 
     */
    <T> Referer<T> refer(Class<T> clz, URL url, URL serviceUrl);

    /**
     * <pre>
	 * 		1） exporter destroy
	 * 		2） referer destroy
	 * </pre>
     * 
     */
    void destroy();
}
