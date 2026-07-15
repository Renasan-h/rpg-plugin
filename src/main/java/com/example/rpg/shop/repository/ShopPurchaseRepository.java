package com.example.rpg.shop.repository;

import com.example.rpg.shop.repository.interfaces.IShopPurchaseRepository;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * SHOP購入履歴を管理するRepository
 *
 * <p>shop-purchases.ymlの読み書きと、プレイヤー別・商品別の購入回数管理を担当する。
 * 購入制限判定に必要な永続データをServiceから分離するためのRepository。</p>
 */
public class ShopPurchaseRepository implements IShopPurchaseRepository {

    /**
     * プラグインインスタンス。
     *
     * <p>dataFolder取得とログ出力に必要なため保持する。</p>
     */
    private final JavaPlugin plugin;

    /**
     * 購入履歴保存ファイル。
     */
    private final File file;

    /**
     * 購入履歴YAML。
     *
     * <p>現時点ではYAMLを直接保持する。
     * 将来的にDB化する場合は、このRepositoryの実装だけ差し替える。</p>
     */
    private YamlConfiguration config;

    /**
     * ShopPurchaseRepositoryを生成する。
     *
     * @param plugin plugin
     * @param file   config file
     */
    public ShopPurchaseRepository(JavaPlugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        load();
    }

    /**
     * {@inheritDoc}
     */
    public int findPurchaseCount(UUID playerId, String itemId) {
        return config.getInt("players." + playerId + "." + itemId, 0);
    }

    /**
     * {@inheritDoc}
     */
    public void incrementPurchaseCount(UUID playerId, String itemId) {
        int current = findPurchaseCount(playerId, itemId);
        config.set("players." + playerId + "." + itemId, current + 1);
        save();
    }

    /**
     * {@inheritDoc}
     */
    public void resetPlayer(UUID playerId) {
        config.set("players." + playerId, null);
        save();
    }

    /**
     * {@inheritDoc}
     */
    public void resetItem(UUID playerId, String itemId) {
        config.set("players." + playerId + "." + itemId, null);
        save();
    }

    /**
     * {@inheritDoc}
     */
    public void resetAll() {
        config.set("players", null);
        save();
    }

    /**
     * {@inheritDoc}
     */
    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * {@inheritDoc}
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("shop-purchases.yml の保存に失敗しました: " + e.getMessage());
        }
    }
}