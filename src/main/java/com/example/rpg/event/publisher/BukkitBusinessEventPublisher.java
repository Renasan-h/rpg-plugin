package com.example.rpg.event.publisher;

import com.example.rpg.event.BusinessEvent;
import org.bukkit.plugin.PluginManager;

import java.util.Objects;

/**
 * Bukkitのイベント機構を使用するBusinessEventPublisher。
 *
 * <p>
 * 発行されたBusinessEventを
 * {@link PluginManager#callEvent(org.bukkit.event.Event)}
 * へ渡す。
 * </p>
 */
public final class BukkitBusinessEventPublisher implements BusinessEventPublisher {

    /**
     * Bukkit PluginManager。
     */
    private final PluginManager pluginManager;

    /**
     * BukkitBusinessEventPublisherを生成する。
     *
     * @param pluginManager Bukkit PluginManager
     * @throws NullPointerException pluginManagerがnullの場合
     */
    public BukkitBusinessEventPublisher(
            final PluginManager pluginManager
    ) {
        this.pluginManager = Objects.requireNonNull(
                pluginManager,
                "pluginManager must not be null"
        );
    }

    /**
     * BusinessEventをBukkitイベントとして発行する。
     *
     * @param event 発行するイベント
     * @throws NullPointerException eventがnullの場合
     */
    @Override
    public void publish(final BusinessEvent event) {
        pluginManager.callEvent(
                Objects.requireNonNull(
                        event,
                        "event must not be null"
                )
        );
    }
}
