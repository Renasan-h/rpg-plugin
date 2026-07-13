package com.example.rpg.listener;

import com.example.rpg.common.message.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * サーバー一覧に表示されるサーバー説明クラス
 */
public class ServerPingListener implements Listener {

    /**
     * サーバー一覧に表示されたタイミングでonPingが実行されるイベント処理
     *
     * @param event サーバーに対してPing応答が投げられたときに発生するイベント
     */
    @EventHandler
    public void onPing(ServerListPingEvent event) {

        event.motd(MessageUtil.mm("""
                <gradient:#00FFFF:#FF00FF>⚔ RPG Adventure Server ⚔</gradient>
                <rainbow>開発中</rainbow>
                <yellow>🏦 銀行</yellow><green>📦 オークション</green>
                """));
    }
}