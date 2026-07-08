package com.example.rpg.repository;

import com.example.rpg.repository.interfaces.IMoneyRepository;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MoneyRepository implements IMoneyRepository {

    /**
     * メモリ上に保持するプレイヤー所持金。
     *
     * <p>頻繁に参照される値のため、毎回YAMLを読むのではなくメモリに保持する。</p>
     */
    private final Map<UUID, Integer> moneyMap = new HashMap<>();

    /**
     * プラグインインスタンス。
     *
     * <p>dataFolder取得とログ出力に必要なため保持する。</p>
     */
    private final JavaPlugin plugin;

    /**
     * 所持金保存ファイル。
     */
    private final File file;

    /**
     * MoneyRepositoryを生成する。
     *
     * @param plugin plugin
     * @param file   config file
     */
    public MoneyRepository(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        load();
    }

    /**
     * {@inheritDoc}
     */
    public int findMoney(UUID playerId) {
        return moneyMap.getOrDefault(playerId, 0);
    }

    /**
     * {@inheritDoc}
     */
    public int addMoney(UUID playerId, int amount) {
        int updated = findMoney(playerId) + amount;
        moneyMap.put(playerId, updated);
        save();
        return updated;
    }

    /**
     * {@inheritDoc}
     */
    public int setMoney(UUID playerId, int amount) {
        moneyMap.put(playerId, amount);
        save();
        return amount;
    }

    /**
     * {@inheritDoc}
     */
    public boolean subtractMoney(UUID playerId, int amount) {
        int current = findMoney(playerId);

        if (current < amount) {
            return false;
        }

        moneyMap.put(playerId, current - amount);
        save();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void load() {
        moneyMap.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.getConfigurationSection("players") == null) {
            return;
        }

        for (String key : config.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            int money = config.getInt("players." + key + ".money");
            moneyMap.put(uuid, money);
        }
    }

    /**
     * {@inheritDoc}
     */
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
