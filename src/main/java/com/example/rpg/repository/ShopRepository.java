package com.example.rpg.repository;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.dto.ShopItemType;
import com.example.rpg.util.RpgUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopRepository extends AbstractRepository<ShopItemDto> {

    private ShopDto shop;

    public ShopRepository() {
    }

    /**
     * 設定ファイルを読み込み {@link ShopDto} を生成し保持する
     *
     * @param config 設定ファイル(config.yml
     */
    public void load(FileConfiguration config) {
        clear();
        ConfigurationSection shopSection = config.getConfigurationSection("shop");

        if (shopSection == null) {
            throw new IllegalStateException("config.yml に shop がありません");
        }

        String title = shopSection.getString("title", "<gold>SHOP</gold>");
        int size = shopSection.getInt("size", 27);

        Map<String, ShopCategoryDto> categories = new LinkedHashMap<>();

        ConfigurationSection categoriesSection = shopSection.getConfigurationSection("categories");

        if (categoriesSection != null) {
            for (String categoryId : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryId);

                if (categorySection == null) {
                    continue;
                }

                ShopCategoryDto category = loadCategory(categoryId, categorySection);
                categories.put(categoryId, category);

                for (ShopItemDto item : category.getItems().values()) {
                    put(item.getId(), item);
                }
            }
        }

        this.shop = new ShopDto(title, size, categories);
    }

    /**
     * 設定ファイル（config.yml）より取得した、情報をもとに各種アイテム情報を取得し {@link ShopCategoryDto} へ格納する
     *
     * @param id      categories配下のキー
     * @param section config.ymlのcategories配下セクション
     * @return {@link ShopCategoryDto} ショップカテゴリ情報
     */
    private ShopCategoryDto loadCategory(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        int slot = RpgUtil.getIntOrDefault(section, "slot", 0);

        String iconText = section.getString("icon", "CHEST");
        Material icon = Material.matchMaterial(iconText);

        if (icon == null) {
            throw new IllegalArgumentException("不正な category icon です： " + iconText + " / categoryId=" + id);
        }

        Map<String, ShopItemDto> items = new LinkedHashMap<>();

        ConfigurationSection itemsSection = section.getConfigurationSection("items");

        if (itemsSection != null) {
            for (String itemId : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);

                if (itemSection == null) {
                    continue;
                }

                ShopItemDto item = loadItem(itemId, itemSection);
                items.put(itemId, item);
            }
        }

        return new ShopCategoryDto(id, name, slot, icon, items);
    }

    /**
     * 設定ファイル（config.yml）より取得した、情報をもとに各種アイテム情報を取得しShopItemDtoへ格納する
     *
     * @param id      item配下のキー
     * @param section config.ymlのitems配下セクション
     * @return {@link ShopItemDto} ショップアイテム情報
     */
    private ShopItemDto loadItem(String id, ConfigurationSection section) {
        int slot = RpgUtil.getIntOrDefault(section, "slot", 0);

        String typeText = section.getString("type", "ITEM");
        ShopItemType type = ShopItemType.valueOf(typeText.toUpperCase());

        String materialText = section.getString("material", "STONE");
        Material material = Material.matchMaterial(materialText);

        if (material == null) {
            throw new IllegalArgumentException("不正な material です: " + materialText + " / itemId=" + id);
        }

        String name = section.getString("name", id);
        String color = section.getString("dispColor", "white");
        String label = section.getString("dispLabel", "<#dispColor#>#name#</#dispColor#>");
        int price = section.getInt("price", 0);
        // 売却額の指定がない場合は1/3の価格で設定する
        int sellPrice = RpgUtil.getIntOrDefault(section, "sellPrice", price == 0 ? 0 : Math.round((float) price / 3));
        int amount = RpgUtil.getIntOrDefault(section, "amount", 1);
        int limit = RpgUtil.getIntOrDefault(section, "limit", -1);

        String permission = section.getString("permission", "");

        // 値段:priceと売値:sellPriceを自身の値で書き換える
        List<String> lore = section.getStringList("lore").stream()
                .map(s -> s.replaceAll("#price#", Integer.toString(price)).replaceAll("#sellPrice#", Integer.toString(price))).toList();
        List<String> commands = section.getStringList("commands");

        return new ShopItemDto(
                id,
                slot,
                type,
                material,
                name,
                color,
                label,
                price,
                sellPrice,
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