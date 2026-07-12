package com.example.rpg.menu.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * SHOP GUIで使用するPersistentDataContainerのキーを管理する。
 *
 * <p>
 * NamespacedKeyの生成を一か所へ集約し、
 * MenuとFacadeで同じキーを使用できるようにする。
 * </p>
 */
public class ShopPdcKeys {

    /**
     * カテゴリIDを保存するキー。
     */
    private final NamespacedKey shopCategoryKey;

    /**
     * 商品IDを保存するキー。
     */
    private final NamespacedKey shopItemKey;

    /**
     * ページ番号を保存するキー。
     */
    private final NamespacedKey pageKey;

    /**
     * GUIアクションを保存するキー。
     */
    private final NamespacedKey actionKey;

    /**
     * SHOP GUI用PDCキーを生成する。
     *
     * @param plugin プラグインインスタンス
     */
    public ShopPdcKeys(final JavaPlugin plugin) {
        this.shopCategoryKey =
                new NamespacedKey(plugin, "shop_category");
        this.shopItemKey =
                new NamespacedKey(plugin, "shop_item");
        this.pageKey =
                new NamespacedKey(plugin, "page");
        this.actionKey =
                new NamespacedKey(plugin, "action");
    }

    /**
     * カテゴリID用キーを取得する。
     *
     * @return カテゴリID用キー
     */
    public NamespacedKey getShopCategoryKey() {
        return shopCategoryKey;
    }

    /**
     * 商品ID用キーを取得する。
     *
     * @return 商品ID用キー
     */
    public NamespacedKey getShopItemKey() {
        return shopItemKey;
    }

    /**
     * ページ番号用キーを取得する。
     *
     * @return ページ番号用キー
     */
    public NamespacedKey getPageKey() {
        return pageKey;
    }

    /**
     * GUIアクション用キーを取得する。
     *
     * @return GUIアクション用キー
     */
    public NamespacedKey getActionKey() {
        return actionKey;
    }
}
