package com.zero.easyrpc.client.provider;

import com.zero.easyrpc.common.exception.RemotingException;
import com.zero.easyrpc.common.serialization.SerializerFactory;
import com.zero.easyrpc.common.transport.body.AckCustomBody;
import com.zero.easyrpc.common.utils.SystemClock;
import com.zero.easyrpc.netty4.Transporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.zero.easyrpc.common.serialization.SerializerFactory.serializerImpl;

/**
 * provider端 连接registry的管理控制对象
 * 用来处理provider与registry的交互
 */
public class RegistryController {

    private static final Logger logger = LoggerFactory.getLogger(RegistryController.class);

    private DefaultProvider defaultProvider;

    private final Map<Long, MessageNonAck> messagesNonAcks = new ConcurrentHashMap<>();

    public RegistryController(DefaultProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    /**
     * registry的地址，多个地址格式是host1:port1,host2:port2
     * @throws InterruptedException
     * @throws RemotingException
     */
    public void publishedAndStartProvider() throws InterruptedException, RemotingException {
        // stack copy
        List<Transporter> transporters = defaultProvider.getPublishRemotingTransporters();

        if (null == transporters || transporters.isEmpty()) {
            logger.warn("Service is empty, please call DefaultProvider #publishService method.");
            return;
        }

        String address = defaultProvider.getRegistryAddress();

        if (address == null) {
            logger.warn("Registry center address is empty.");
            return;
        }

        String[] addresses = address.split(",");
        if (addresses.length > 0) {
            for (String eachAddress : addresses) {
                for (Transporter service : transporters) {
                    pushPublishServiceToRegistry(service, eachAddress);
                }
            }
        }
    }

    private void pushPublishServiceToRegistry(Transporter service, String registerAddress) throws InterruptedException, RemotingException {

        messagesNonAcks.put(service.getRequestId(), new MessageNonAck(service, registerAddress));
        
        Transporter result = defaultProvider.getNettyClient().invokeSync(registerAddress, service, 3000);
        if (result != null) {
            AckCustomBody ackCustomBody = SerializerFactory.serializerImpl().readObject(result.getBytes(), AckCustomBody.class);

            logger.info("Received ack info [{}]", ackCustomBody);
            if (ackCustomBody.isSuccess()) {
                messagesNonAcks.remove(ackCustomBody.getRequestId());
            }
            logger.info("Publish service {} to Registry.", service);
        } else {
            logger.warn("registry center handler timeout");
        }
    }

    public void checkPublishFailMessage() throws InterruptedException, RemotingException {
        if (messagesNonAcks.keySet() != null && messagesNonAcks.keySet().size() > 0) {
            logger.warn("have [{}] message send failed,send again", messagesNonAcks.keySet().size());
            for (MessageNonAck ack : messagesNonAcks.values()) {
                pushPublishServiceToRegistry(ack.getMsg(), ack.getAddress());
            }
        }
    }


    static class MessageNonAck {

        private final long id;

        private final Transporter msg;
        private final String address;
        private final long timestamp = SystemClock.millisClock().now();

        public MessageNonAck(Transporter msg, String address) {
            this.msg = msg;
            this.address = address;

            id = msg.getRequestId();
        }

        public long getId() {
            return id;
        }

        public Transporter getMsg() {
            return msg;
        }

        public String getAddress() {
            return address;
        }

        public long getTimestamp() {
            return timestamp;
        }

    }

}
