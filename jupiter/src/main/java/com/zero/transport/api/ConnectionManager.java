package com.zero.transport.api;


import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.util.Maps;
import com.zero.transport.Connection;
import com.zero.transport.UnresolvedAddress;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  连接管理器, 用于自动管理(按照地址归组)连接.
 */
public class ConnectionManager {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectionManager.class);

    private final ConcurrentMap<UnresolvedAddress, CopyOnWriteArrayList<Connection>> connections = Maps.newConcurrentMap();

    public void manage(Connection connection) {
        UnresolvedAddress address = connection.getAddress();
        CopyOnWriteArrayList<Connection> list = connections.get(address);
        if (list == null) {
            CopyOnWriteArrayList<Connection> newList = new CopyOnWriteArrayList<>();
            list = connections.putIfAbsent(address, newList);
            if (list == null) {
                list = newList;
            }
        }
        list.add(connection);
    }

    /**
     * 取消自动重连
     */
    public void cancelReconnect(UnresolvedAddress address) {
        CopyOnWriteArrayList<Connection> list = connections.remove(address);
        if (list != null) {
            for (Connection c : list) {
                c.setReconnect(false);
            }
            logger.warn("Cancel reconnect to: {}.", address);
        }
    }

    /**
     * 取消对所有地址的自动重连
     */
    public void cancelAllReconnect() {
        for (UnresolvedAddress address : connections.keySet()) {
            cancelReconnect(address);
        }
    }
}
