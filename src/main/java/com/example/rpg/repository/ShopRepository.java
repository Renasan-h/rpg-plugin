package com.example.rpg.repository;

import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.dto.ShopItemType;
import com.example.rpg.util.RpgUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ShopRepository extends AbstractRepository<ShopItemDto> {

    private ShopDto shop;

    public ShopRepository() {
    }

    public void load(FileConfiguration config) {
        clear();
        ConfigurationSection shopSection = config.getConfigurationSection("shop");

        if (shopSection == null) {
            throw new IllegalStateException("config.yml に shop がありません");
        }

        String title = shopSection.getString("title", "<gold>SHOP</gold>");
        int size = shopSection.getInt("size", 27);

        ConfigurationSection itemsSection = shopSection.getConfigurationSection("items");

        if (itemsSection != null) {
            for (String id : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(id);

                if (itemSection == null) {
                    continue;
                }

                ShopItemDto item = loadItem(id, itemSection);
                put(id, item);
            }
        }

        this.shop = new ShopDto(title, size, findAllAsMap());
    }

    private ShopItemDto loadItem(String id, ConfigurationSection section) {
        int slot = section.getInt("slot");

        String typeText = section.getString("type", "ITEM");
        ShopItemType type = ShopItemType.valueOf(typeText.toUpperCase());

        String materialText = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialText);

        if (material == null) {
            throw new IllegalArgumentException("不正な material です: " + materialText + " / itemId=" + id);
        }

        String name = section.getString("name", id);
        int price = section.getInt("price", 0);
        int amount = RpgUtil.getIntOrDefault(section, "amount", 1);
        int limit = RpgUtil.getIntOrDefault(section, "limit", -1);

        String permission = section.getString("permission", "");

        List<String> lore = section.getStringList("lore");
        List<String> commands = section.getStringList("commands");

        return new ShopItemDto(
                id,
                slot,
                type,
                material,
                name,
                price,
                amount,
                limit,
                permission,
                lore,
                commands
        );
    }

    public ShopDto getShop() {
        return shop;
    }
}