package com.zero.registry;

/**
 * Service subscribers listener.
 *
 */
public interface NotifyListener {

    void notify(RegisterMeta registerMeta, NotifyEvent event);

    enum NotifyEvent {
        ADDED,
        REMOVED
    }
}
