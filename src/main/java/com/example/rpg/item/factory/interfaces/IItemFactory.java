package com.example.rpg.item.factory.interfaces;

import org.bukkit.inventory.ItemStack;

/**
 * ItemStack生成インターフェース
 *
 * <p>アイテムIDからItemStackを生成する責務を持つ。</p>
 */
public interface IItemFactory {

    /**
     * アイテムを1個生成する。
     *
     * @param itemId アイテムID
     * @return ItemStack
     */
    ItemStack create(String itemId);

    /**
     * 指定戸数のアイテムを生成する。
     *
     * @param itemId アイテムID
     * @param amount 個数
     * @return ItemStack
     */
    ItemStack create(String itemId, int amount);
}
