package com.example.rpg.event.publisher;

import com.example.rpg.event.BusinessEvent;

/**
 * BusinessEventを発行するPublisher。
 *
 * <p>
 * Serviceなどのイベント発行元が、BukkitのPluginManagerへ
 * 直接依存しないための抽象化を提供する。
 * </p>
 */
public interface BusinessEventPublisher {

    /**
     * BusinessEventを発行する。
     *
     * @param event 発行するイベント
     * @throws NullPointerException eventがnullの場合
     */
    void publish(BusinessEvent event);
}
