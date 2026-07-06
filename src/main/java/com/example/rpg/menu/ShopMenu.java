package com.example.rpg.menu;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.ShopRepository;
import com.example.rpg.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopMenu {

    private static final String CATEGORY_TILE = "<gold>RPG SHOP</gold>";
    private static final String ITEM_TITLE_PREFIX = "<gold>RPG SHOP</gold> <gray>-</gray>";

    private final ShopRepository shopRepository;

    public ShopMenu(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public static String categoryTitle() {
        return CATEGORY_TILE;
    }

    public static String itemTitlePrefix() {
        return ITEM_TITLE_PREFIX;
    }

    public void open(Player player) {
        ShopDto shop = shopRepository.getShop();

        Inventory inventory = Bukkit.createInventory(null, shop.getSize(), MessageUtil.mm(CATEGORY_TILE));

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
     * ショップカテゴリ一覧を開く
     *
     * @param player   操作プレイヤー情報
     * @param category ショップカテゴリ情報
     */
    public void openCategory(Player player, ShopCategoryDto category) {
        ShopDto shop = shopRepository.getShop();

        Inventory inventory = Bukkit.createInventory(
                null,
                shop.getSize(),
                MessageUtil.mm(ITEM_TITLE_PREFIX + category.getName())
        );

        for (ShopItemDto item : category.getItems().values()) {
            // 表示対象のアイテムに権限が付与されていた場合は、プレイヤーの権限を確認する
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
    }

}