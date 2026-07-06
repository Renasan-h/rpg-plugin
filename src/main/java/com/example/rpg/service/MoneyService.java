package com.example.rpg.service;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * お金を管理するためのサービス
 * プレイヤーID（UUID)で紐づける
 * TODO: 将来的にDBか何かで管理する
 */
public class MoneyService implements Listener {

    private final Map<UUID, Integer> moneyMap = new HashMap<>();
    private final JavaPlugin plugin;
    private final File file;

    public MoneyService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "money.yaml");
        load();
    }

    public int getMoney(UUID playerId) {
        return moneyMap.getOrDefault(playerId, 0);
    }

    /**
     * お金を追加する
     *
     * @param playerId 対象プレイヤーID
     * @param amount   金額
     * @return 追加後金額
     */
    public int addMoney(UUID playerId, int amount) {
        int updated = getMoney(playerId) + amount;
        moneyMap.put(playerId, updated);
        save();
        return updated;
    }

    /**
     * 金額を設定する
     *
     * @param playerId 対象プレイヤーID
     * @param amount   金額
     * @return 設定金額
     */
    public int setMoney(UUID playerId, int amount) {
        moneyMap.put(playerId, amount);
        save();
        return amount;
    }

    /**
     * お金を指定した額減らす
     *
     * @param playerId 対象プレイヤーID
     * @param amount   金額
     * @return true:完了 false:所持金額よりも減少額が大きい
     */
    public boolean removeMoney(UUID playerId, int amount) {
        int current = getMoney(playerId);

        if (current < amount) {
            return false;
        }

        moneyMap.put(playerId, current - amount);
        save();
        return true;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            save();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        moneyMap.clear();

        if (config.getConfigurationSection("players") == null) {
            return;
        }

        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int money = config.getInt("players." + key + ".money");
            moneyMap.put(uuid, money);
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Integer> entry : moneyMap.entrySet()) {
            config.set("players." + entry.getKey() + ".money", entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("money.yml の保存に失敗しました: " + e.getMessage());
        }
    }
}
