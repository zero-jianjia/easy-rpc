package com.zero.easyrpc.client.consumer;

import com.zero.easyrpc.common.loadbalance.LoadBalanceStrategy;
import com.zero.easyrpc.common.protocal.Protocol;
import com.zero.easyrpc.common.rpc.RegisterMeta;
import com.zero.easyrpc.common.transport.body.AckCustomBody;
import com.zero.easyrpc.common.transport.body.SubcribeResultCustomBody;
import com.zero.easyrpc.netty4.Transporter;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.zero.easyrpc.common.serialization.SerializerFactory.serializerImpl;

/**
 * 消费者端的一些逻辑处理
 * Created by jianjia1 on 17/12/07.
 */
public class ConsumerManager {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerManager.class);

    private DefaultConsumer defaultConsumer;     //consumer模块的代码手持defaultConsumer好办事
    private final ReentrantReadWriteLock registriesLock = new ReentrantReadWriteLock();
    private final Map<String, List<RegisterMeta>> registries = new ConcurrentHashMap<String, List<RegisterMeta>>();

    public ConsumerManager(DefaultConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    /**
     * 处理服务的订阅结果
     * @param request
     * @param channel
     * @return
     */
    public Transporter handlerSubcribeResult(Transporter request, Channel channel) {

        if (logger.isDebugEnabled()) {
            logger.debug("handler subcribe result [{}] and channel [{}]", request, channel);
        }

        AckCustomBody ackCustomBody = new AckCustomBody(request.getRequestId(), false);
        Transporter responseTransporter = Transporter.createResponseTransporter(Protocol.ACK, ackCustomBody, request.getRequestId());

        SubcribeResultCustomBody subcribeResultCustomBody = serializerImpl().readObject(request.getBytes(), SubcribeResultCustomBody.class);

        String serviceName = null;
        if (subcribeResultCustomBody != null && subcribeResultCustomBody.getRegisterMeta() != null && !subcribeResultCustomBody.getRegisterMeta().isEmpty()) {

            for (RegisterMeta registerMeta : subcribeResultCustomBody.getRegisterMeta()) {

                if (null == serviceName) {
                    serviceName = registerMeta.getServiceName();
                }
                notify(serviceName, registerMeta, NotifyListener.NotifyEvent.CHILD_ADDED);
            }
        }

        ackCustomBody.setSuccess(true);
        return responseTransporter;
    }

    /**
     * 处理服务取消的时候逻辑处理
     * @param request
     * @param channel
     * @return
     */
    public Transporter handlerSubscribeResultCancel(Transporter request, Channel channel) {
        AckCustomBody ackCustomBody = new AckCustomBody(request.getRequestId(), false);
        Transporter responseTransporter = Transporter.createResponseTransporter(Protocol.ACK, ackCustomBody, request.getRequestId());

        SubcribeResultCustomBody subcribeResultCustomBody = serializerImpl().readObject(request.getBytes(), SubcribeResultCustomBody.class);

        if (subcribeResultCustomBody != null && subcribeResultCustomBody.getRegisterMeta() != null && !subcribeResultCustomBody.getRegisterMeta().isEmpty()) {

            for (RegisterMeta registerMeta : subcribeResultCustomBody.getRegisterMeta()) {
                notify(registerMeta.getServiceName(), registerMeta, NotifyListener.NotifyEvent.CHILD_REMOVED);
            }
        }
        ackCustomBody.setSuccess(true);
        return responseTransporter;
    }

    /**
     * 处理注册中心发送过来的负载均衡策略的变化
     * @param request
     * @param channel
     * @return
     */
    public Transporter handlerServiceLoadBalance(Transporter request, Channel channel) {

        if (logger.isDebugEnabled()) {
            logger.debug("handler change loadBalance strategy [{}] and channel [{}]", request, channel);
        }

        AckCustomBody ackCustomBody = new AckCustomBody(request.getRequestId(), false);
        Transporter responseTransporter = Transporter.createResponseTransporter(Protocol.ACK, ackCustomBody, request.getRequestId());

        SubcribeResultCustomBody subcribeResultCustomBody = serializerImpl().readObject(request.getBytes(), SubcribeResultCustomBody.class);

        String serviceName = subcribeResultCustomBody.getServiceName();

        LoadBalanceStrategy balanceStrategy = subcribeResultCustomBody.getLoadBalanceStrategy();

        defaultConsumer.setServiceLoadBalanceStrategy(serviceName, balanceStrategy);

        ackCustomBody.setSuccess(true);
        return responseTransporter;
    }

    /*************************
     * ↑为核心方法，下面为内部方法
     ************************/

    private void notify(String serviceName, RegisterMeta registerMeta, NotifyListener.NotifyEvent event) {

        boolean notifyNeeded = false;

        final Lock writeLock = registriesLock.writeLock();
        writeLock.lock();
        try {
            List<RegisterMeta> registerMetas = registries.get(serviceName);
            if (registerMetas == null) {
                if (event == NotifyListener.NotifyEvent.CHILD_REMOVED) {
                    return;
                }
                registerMetas = new ArrayList<RegisterMeta>();
                registerMetas.add(registerMeta);
                notifyNeeded = true;
            } else {
                if (event == NotifyListener.NotifyEvent.CHILD_REMOVED) {
                    registerMetas.remove(registerMeta);
                } else if (event == NotifyListener.NotifyEvent.CHILD_ADDED) {
                    registerMetas.add(registerMeta);
                }
                notifyNeeded = true;
            }
            registries.put(serviceName, registerMetas);
        } finally {
            writeLock.unlock();
        }

        if (notifyNeeded) {

            NotifyListener listener = this.defaultConsumer.getDefaultConsumerRegistry().getServiceMatchedNotifyListener().get(serviceName);
            if (null != listener) {
                listener.notify(registerMeta, event);
            }

        }
    }

}