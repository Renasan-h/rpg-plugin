package com.example.rpg.menu.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * SHOP商品一覧画面を識別するためのInventoryHolder。
 * <p>
 * カテゴリ別商品画面を保持
 * クリック処理ではこのcategoryIdを使って購入対象カテゴリを特定する
 *
 */
public class ItemMenuHolder implements InventoryHolder {

    /**
     * 表示中カテゴリID。
     */
    private final String categoryId;

    /**
     * コンストラクタ。
     *
     * @param categoryId 表示対象カテゴリID
     */
    public ItemMenuHolder(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 表示中カテゴリIDを返す。
     *
     * @return categoryId 表示中カテゴリID
     */
    public String getCategoryId() {
        return categoryId;
    }

    /**
     * このHolderは画面識別用であり、Inventory実体の保持は不要なため、
     * このメソッドが呼ばれた場合は例外を投げる
     *
     * @return この実装では返却しない
     * @throws UnsupportedOperationException
     */
    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException("ItemMenuHolder does not store inventory instance.");
    }
}