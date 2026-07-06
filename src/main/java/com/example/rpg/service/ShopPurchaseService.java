package com.example.rpg.service;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ShopPurchaseService {

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public ShopPurchaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shop-purchases.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public int getPurchaseCount(UUID playerId, String itemKey) {
        return config.getInt("players." + playerId + "." + itemKey, 0);
    }

    public void addPurchaseCount(UUID playerId, String itemKey) {
        int current = getPurchaseCount(playerId, itemKey);
        config.set("players." + playerId + "." + itemKey, current + 1);
        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("shop-purchases.yml の保存に失敗しました: " + e.getMessage());
        }
    }

    public void resetPlayer(UUID playerId) {
        config.set("players." + playerId, null);
        save();
    }

    public void resetItem(UUID playerId, String itemKey) {
        config.set("players." + playerId + "." + itemKey, null);
        save();
    }
}
