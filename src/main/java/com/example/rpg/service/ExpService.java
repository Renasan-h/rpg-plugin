package com.example.rpg.service;

import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * 経験値を管理するためのサービス
 * プレイヤーID（UUID)で紐づける
 * TODO: 将来的にDBか何かで管理する
 */
public class ExpService implements Listener {

    private final Map<UUID, Integer> expMap = new HashMap<>();

    public int getExp(UUID playerId){
        return expMap.getOrDefault(playerId, 0);
    }

    public int addExp(UUID playerId, int expValue) {
        int current = getExp(playerId);
        int updated = current + expValue;
        expMap.put(playerId, updated);
        return updated;
    }
}
