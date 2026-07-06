package com.example.rpg.listener;

import org.bukkit.event.server.ServerListPingEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * サーバー一覧に表示されるサーバー説明クラス
 */
public class ServerPingListener implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();

    /**
     * サーバー一覧に表示されたタイミングでonPingが実行されるイベント処理
     * @param event サーバーに対してPing応答が投げられたときに発生するイベント
     */
    @EventHandler
    public void onPing(ServerListPingEvent event) {

        event.motd(mm.deserialize("""
                <gradient:#00FFFF:#FF00FF>⚔ RPG Adventure Server ⚔</gradient>
                <yellow>🏦 銀行</yellow><green>📦 オークション</green>
                """));
    }
}