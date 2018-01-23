package com.zero.easyrpc.config.handler;


import com.zero.easyrpc.cluster.Cluster;
import com.zero.easyrpc.rpc.Exporter;
import com.zero.easyrpc.rpc.URL;

import java.util.Collection;
import java.util.List;

/**
 * 
 * Handle urls which are from config.
 *
 * @author fishermen
 * @version V1.0 created at: 2013-5-31
 */
public interface ConfigHandler {

//    <T> ClusterSupport<T> buildClusterSupport(Class<T> interfaceClass, List<URL> registryUrls);

    <T> T refer(Class<T> interfaceClass, List<Cluster<T>> cluster, String proxyType);

    <T> Exporter<T> export(Class<T> interfaceClass, T ref, List<URL> registryUrls);

    <T> void unexport(List<Exporter<T>> exporters, Collection<URL> registryUrls);
}
