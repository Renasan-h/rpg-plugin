package com.example.rpg.menu.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
     * 現在のページへ表示している商品ID。
     *
     * <p>リストのインデックスとGUIスロットを対応させる。</p>
     */
    private final List<String> displayedItemIds;

    /**
     * ItemMenuHolderを生成する。
     *
     * @param categoryId       表示対象カテゴリID
     * @param page             表示ページ番号
     * @param displayedItemIds 表示中の商品ID
     */
    public ItemMenuHolder(
            final String categoryId,
            final int page,
            final List<String> displayedItemIds) {
        this.categoryId = categoryId;
        this.page = page;
        this.displayedItemIds = displayedItemIds;
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
     * クリックされたスロットに対応する商品IDを取得する。
     *
     * @param slot クリックされたスロット
     * @return 商品ID。商品スロットでない場合はnull
     */
    public @Nullable String findItemIdBySlot(final int slot) {
        if (slot < 0 || slot >= displayedItemIds.size()) {
            return null;
        }

        return displayedItemIds.get(slot);
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