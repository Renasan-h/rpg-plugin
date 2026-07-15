package com.example.rpg.item.service;

import com.example.rpg.item.pdc.ItemPdcKeys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

/**
 * RPGアイテムのPersistentDataContainerを操作するService
 */
public class ItemPdcService {

    /**
     * RPGアイテム用PDCキー
     */
    private final ItemPdcKeys pdcKeys;

    /**
     * ItemPdcServiceを生成する。
     *
     * @param pdcKeys RPGアイテム用PDCキー
     */
    public ItemPdcService(final ItemPdcKeys pdcKeys) {
        this.pdcKeys = Objects.requireNonNull(
                pdcKeys,
                "pdcKeys must not be null"
        );
    }

    /**
     * ItemStackからRPGアイテムIDを取得する。
     *
     * @param itemStack 取得対象アイテム
     * @return RPGアイテムID 設定されていない場合はnull
     */
    public String getItemId(final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }

        final ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return null;
        }

        return meta.getPersistentDataContainer().get(
                pdcKeys.getItemIdKey(),
                PersistentDataType.STRING
        );
    }
}