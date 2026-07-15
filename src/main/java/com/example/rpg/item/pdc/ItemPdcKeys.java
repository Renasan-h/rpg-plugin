package com.example.rpg.item.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * RPGアイテムで使用するPersistentDataContainerのキーを管理する
 */
public class ItemPdcKeys {

    /**
     * RPGアイテムIDを保存するキー
     */
    private final NamespacedKey itemIdKey;

    /**
     * ItemPdcKeysを生成する
     *
     * @param plugin プラグインインスタンス
     */
    public ItemPdcKeys(final JavaPlugin plugin) {
        final JavaPlugin requiredPlugin = Objects.requireNonNull(
                plugin,
                "plugin must not be null"
        );

        this.itemIdKey = new NamespacedKey(
                requiredPlugin,
                "item_id"
        );
    }

    /**
     * RPGアイテムID用のキーを取得する。
     *
     * @return RPGアイテムID用キー
     */
    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }
}
