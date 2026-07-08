package com.example.rpg.menu;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.menu.holder.CategoryMenuHolder;
import com.example.rpg.menu.holder.ItemMenuHolder;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopMenu {

    /**
     * カテゴリ一覧画面のタイトル。
     */
    private static final String CATEGORY_TITLE = "<gold>RPG SHOP</gold>";

    /**
     * 商品一覧画面タイトルの接頭辞。
     */
    private static final String ITEM_TITLE_PREFIX = "<gold>RPG SHOP</gold> <gray>-</gray> ";

    /**
     * SHOP設定情報の取得元。
     */
    private final IShopRepository shopRepositoryImpl;

    /**
     * コンストラクタ。
     *
     * @param shopRepositoryImpl SHOP設定Repository
     */
    public ShopMenu(IShopRepository shopRepositoryImpl) {
        this.shopRepositoryImpl = shopRepositoryImpl;
    }

    /**
     * SHOPカテゴリ一覧画面を開く。
     *
     * @param player 表示対象プレイヤー
     */
    public void openShopCategory(Player player) {
        ShopDto shop = shopRepositoryImpl.getShop();

        Inventory inventory = Bukkit.createInventory(
                new CategoryMenuHolder(), shop.getSize(), MessageUtil.mm(CATEGORY_TITLE));

        for (ShopCategoryDto category : shop.getCategories().values()) {
            ItemStack icon = new ItemStack(category.getIcon());
            ItemMeta meta = icon.getItemMeta();

            meta.displayName(MessageUtil.mm(category.getName()));
            icon.setItemMeta(meta);

            inventory.setItem(category.getSlot(), icon);
        }

        player.openInventory(inventory);
    }

    /**
     * 指定カテゴリの商品一覧画面を開く。
     *
     * @param player   表示対象プレイヤー
     * @param category 表示対象カテゴリ
     */
    public void openShopItemByCategory(Player player, ShopCategoryDto category) {
        ShopDto shop = shopRepositoryImpl.getShop();

        Inventory inventory = Bukkit.createInventory(
                new ItemMenuHolder(category.getId()),
                shop.getSize(),
                MessageUtil.mm(ITEM_TITLE_PREFIX + category.getName())
        );

        for (ShopItemDto item : category.getItems().values()) {
            // 権限付き商品は、権限を持つプレイヤーにだけ表示する。
            if (!item.getPermission().isEmpty() && !player.hasPermission(item.getPermission())) {
                continue;
            }

            ItemStack itemStack = new ItemStack(item.getMaterial(), item.getAmount());
            ItemMeta meta = itemStack.getItemMeta();

            meta.displayName(MessageUtil.mm(item.getDisplayName()));
            meta.lore(item.getLore().stream().map(MessageUtil::mm).toList());

            itemStack.setItemMeta(meta);
            inventory.setItem(item.getSlot(), itemStack);
        }

        player.openInventory(inventory);
    }

}