package com.example.rpg.menu.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * SHOPカテゴリ一覧画面を識別するためのInventoryHolder
 *
 * <p>タイトル文字列でGUIを判定すると、表示名変更や多言語化で処理が壊れるため、
 * Bukkitが提供するInventoryHolderを使って画面種別を判定する。</p>
 */
public class CategoryMenuHolder implements InventoryHolder {

    /**
     * Interface method
     *
     * @return この実装では返却しない
     * @throws UnsupportedOperationException
     */
    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("CategoryMenuHolder does not store inventory instance.");
    }
}