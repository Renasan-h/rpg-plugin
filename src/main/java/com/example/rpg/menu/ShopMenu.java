package com.example.rpg.menu;

import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.repository.ShopRepository;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ShopMenu {

    private final ShopRepository shopRepository;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ShopMenu(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public void open(Player player) {
        ShopDto shop = shopRepository.getShop();

        Inventory inventory = Bukkit.createInventory(null, shop.getSize(), mm.deserialize(shop.getTitle()));
        Map<String, ShopItemDto> items = shop.getItems();

        if (items != null) {
            for (ShopItemDto item : items.values()) {
                if (!player.hasPermission(item.getPermission())) continue;
                if (item.getMaterial() == null) continue;

                ItemStack itmStk = new ItemStack(item.getMaterial(), item.getAmount());
                ItemMeta meta = itmStk.getItemMeta();

                meta.displayName(mm.deserialize(item.getName()));
                meta.lore(item.getLore().stream()
                        .map(mm::deserialize)
                        .toList());
                itmStk.setItemMeta(meta);
                inventory.setItem(item.getSlot(), itmStk);
            }
        }

        player.openInventory(inventory);
    }
}