package com.zero.registry.api;


import com.zero.registry.RegisterMeta;
import io.netty.util.internal.ConcurrentSet;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.zero.common.concurrent.NamedThreadFactory;
import org.zero.common.util.Lists;
import org.zero.common.util.Maps;
import com.zero.registry.NotifyListener;
import com.zero.registry.OfflineListener;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.zero.common.util.StackTraceUtil.stackTrace;

public abstract class AbstractRegistryService implements RegistryService {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractRegistryService.class);

    // 要注册的服务先放在队列里面
    private final LinkedBlockingQueue<RegisterMeta> queue = new LinkedBlockingQueue<>();

    private final ExecutorService registerExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("registry.executor"));
    private final ScheduledExecutorService registerScheduledExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry.schedule.executor"));
    private final ExecutorService localRegisterWatchExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("local.registry.watch.executor"));

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * 每个ServiceMeta对应的 provider信息
     */
    private final ConcurrentMap<RegisterMeta.ServiceMeta, RegisterValue> registries = Maps.newConcurrentMap();

    private final ConcurrentMap<RegisterMeta.ServiceMeta, CopyOnWriteArrayList<NotifyListener>> subscribeListeners = Maps.newConcurrentMap();

    /**
     * 下线Listener
     */
    private final ConcurrentMap<RegisterMeta.Address, CopyOnWriteArrayList<OfflineListener>> offlineListeners = Maps.newConcurrentMap();

    /**
     * Consumer已订阅的信息
     */
    private final ConcurrentSet<RegisterMeta.ServiceMeta> subscribeSet = new ConcurrentSet<>();
    /**
     * Provider已发布的注册信息
     */
    private final ConcurrentMap<RegisterMeta, RegisterState> registerMetaMap = Maps.newConcurrentMap();

    public AbstractRegistryService() {
        registerExecutor.execute(new Runnable() {

            @Override
            public void run() {
                while (!shutdown.get()) {
                    RegisterMeta meta = null;
                    try {
                        meta = queue.take();
                        registerMetaMap.put(meta, RegisterState.PREPARE);
                        doRegister(meta);
                    } catch (Throwable t) {
                        if (meta != null) {
                            logger.error("Register [{}] fail: {}, will try again...", meta.getServiceMeta(), stackTrace(t));

                            // 间隔一段时间再重新入队, 让出cpu
                            final RegisterMeta finalMeta = meta;
                            registerScheduledExecutor.schedule(new Runnable() {
                                @Override
                                public void run() {
                                    queue.add(finalMeta);
                                }
                            }, 1, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        });

        localRegisterWatchExecutor.execute(new Runnable() {

            @Override
            public void run() {
                while (!shutdown.get()) {
                    try {
                        Thread.sleep(3000);
                        doCheckRegisterNodeStatus();
                    } catch (Throwable t) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Check registry node status fail: {}, will try again...", stackTrace(t));
                        }
                    }
                }
            }
        });
    }

    @Override
    public void register(RegisterMeta meta) {
        queue.add(meta);
    }

    @Override
    public void unregister(RegisterMeta meta) {
        if (!queue.remove(meta)) {
            registerMetaMap.remove(meta);
            doUnregister(meta);
        }
    }

    @Override
    public void subscribe(RegisterMeta.ServiceMeta serviceMeta, NotifyListener listener) {
        CopyOnWriteArrayList<NotifyListener> listeners = subscribeListeners.get(serviceMeta);
        if (listeners == null) {
            CopyOnWriteArrayList<NotifyListener> newListeners = new CopyOnWriteArrayList<>();
            listeners = subscribeListeners.putIfAbsent(serviceMeta, newListeners);
            if (listeners == null) {
                listeners = newListeners;
            }
        }
        listeners.add(listener);

        subscribeSet.add(serviceMeta);
        doSubscribe(serviceMeta);
    }

    @Override
    public Collection<RegisterMeta> lookup(RegisterMeta.ServiceMeta serviceMeta) {
        RegisterValue value = registries.get(serviceMeta);

        if (value == null) {
            return Collections.emptyList();
        }

        final Lock readLock = value.lock.readLock();
        readLock.lock();
        try {
            return Lists.newArrayList(value.metaSet);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Map<RegisterMeta.ServiceMeta, Integer> consumers() {
        Map<RegisterMeta.ServiceMeta, Integer> result = Maps.newHashMap();
        for (Map.Entry<RegisterMeta.ServiceMeta, RegisterValue> entry : registries.entrySet()) {
            RegisterValue value = entry.getValue();
            final Lock readLock = value.lock.readLock();
            readLock.lock();
            try {
                result.put(entry.getKey(), value.metaSet.size());
            } finally {
                readLock.unlock();
            }
        }
        return result;
    }

    @Override
    public Map<RegisterMeta, RegisterState> providers() {
        return new HashMap<>(registerMetaMap);
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public void shutdownGracefully() {
        if (!shutdown.getAndSet(true)) {
            registerExecutor.shutdown();
            localRegisterWatchExecutor.shutdown();
            try {
                destroy();
            } catch (Exception ignored) {
            }
        }
    }

    public abstract void destroy();

    public void offlineListening(RegisterMeta.Address address, OfflineListener listener) {
        CopyOnWriteArrayList<OfflineListener> listeners = offlineListeners.get(address);
        if (listeners == null) {
            CopyOnWriteArrayList<OfflineListener> newListeners = new CopyOnWriteArrayList<>();
            listeners = offlineListeners.putIfAbsent(address, newListeners);
            if (listeners == null) {
                listeners = newListeners;
            }
        }
        listeners.add(listener);
    }

    public void offline(RegisterMeta.Address address) {
        // remove & notify
        CopyOnWriteArrayList<OfflineListener> listeners = offlineListeners.remove(address);
        if (listeners != null) {
            for (OfflineListener l : listeners) {
                l.offline();
            }
        }
    }

    // 通知新增或删除服务
    public void notify(RegisterMeta.ServiceMeta serviceMeta, NotifyListener.NotifyEvent event, long version, RegisterMeta... array) {
        if (array == null || array.length == 0) {
            return;
        }

        RegisterValue value = registries.get(serviceMeta);
        if (value == null) {
            RegisterValue newValue = new RegisterValue();
            value = registries.putIfAbsent(serviceMeta, newValue);
            if (value == null) {
                value = newValue;
            }
        }

        boolean notifyNeeded = false;

        final Lock writeLock = value.lock.writeLock();
        writeLock.lock();
        try {
            if (version > value.version) {
                if (event == NotifyListener.NotifyEvent.REMOVED) {
                    for (RegisterMeta m : array) {
                        value.metaSet.remove(m);
                    }
                } else if (event == NotifyListener.NotifyEvent.ADDED) {
                    Collections.addAll(value.metaSet, array);
                }
                value.version = version;
                notifyNeeded = true;
            }
        } finally {
            writeLock.unlock();
        }

        if (notifyNeeded) {
            CopyOnWriteArrayList<NotifyListener> listeners = subscribeListeners.get(serviceMeta);
            if (listeners != null) {
                for (NotifyListener l : listeners) {
                    for (RegisterMeta m : array) {
                        l.notify(m, event);
                    }
                }
            }
        }
    }

    protected abstract void doSubscribe(RegisterMeta.ServiceMeta serviceMeta);

    protected abstract void doRegister(RegisterMeta meta);

    protected abstract void doUnregister(RegisterMeta meta);

    protected abstract void doCheckRegisterNodeStatus();

    public ConcurrentSet<RegisterMeta.ServiceMeta> getSubscribeSet() {
        return subscribeSet;
    }

    public ConcurrentMap<RegisterMeta, RegisterState> getRegisterMetaMap() {
        return registerMetaMap;
    }

    protected static class RegisterValue {
        private long version = Long.MIN_VALUE;
        private final Set<RegisterMeta> metaSet = new HashSet<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    }
}
