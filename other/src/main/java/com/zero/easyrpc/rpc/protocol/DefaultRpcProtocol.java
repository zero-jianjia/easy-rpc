/*
 *  Copyright 2009-2016 Weibo, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.zero.easyrpc.rpc.protocol;


import com.zero.easyrpc.transport.ProviderMessageRouter;
import com.zero.easyrpc.rpc.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author maijunsheng
 * @version 创建时间：2013-5-22
 */
public class DefaultRpcProtocol extends AbstractProtocol {

    // 多个service可能在相同端口进行服务暴露，因此来自同个端口的请求需要进行路由以找到相应的服务，同时不在该端口暴露的服务不应该被找到
    private ConcurrentHashMap<String, ProviderMessageRouter> ipPort2RequestRouter = new ConcurrentHashMap<String, ProviderMessageRouter>();

    @Override
    protected <T> Exporter<T> createExporter(Provider<T> provider, URL url) {
        return new DefaultRpcExporter<T>(provider, url, this.ipPort2RequestRouter, this.exporterMap);
    }

    @Override
    protected <T> Referer<T> createReferer(Class<T> clz, URL url, URL serviceUrl) {
        return new DefaultRpcReferer<T>(clz, url, serviceUrl);
    }

}
