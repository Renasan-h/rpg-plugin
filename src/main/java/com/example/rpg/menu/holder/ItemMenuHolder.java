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
     * 表示中ページ番号。
     */
    private final int page;

    /**
     * ItemMenuHolderを生成する。
     *
     * @param categoryId 表示対象カテゴリID
     * @param page       表示ページ番号
     */
    public ItemMenuHolder(
            final String categoryId,
            final int page) {
        this.categoryId = categoryId;
        this.page = page;
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
     * 表示中ページ番号を取得する。
     *
     * @return ページ番号
     */
    public int getPage() {
        return page;
    }

    /**
     * このHolderは画面識別用であり、Inventory実体は保持しない。
     *
     * @return この実装では返却しない
     * @throws UnsupportedOperationException 常に発生
     */
    @Override
    public @NotNull Inventory getInventory() {
        throw new UnsupportedOperationException(
                "ItemMenuHolder does not store inventory instance."
        );
    }
}