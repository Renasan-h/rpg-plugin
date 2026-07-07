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

    /**
     * カテゴリIDからSHOPカテゴリを取得する。
     *
     * <p>カテゴリの保持形式をRepository内部へ閉じ込めることで、
     * 将来DB化しても呼び出し側の変更を最小化する。</p>
     *
     * @param categoryId カテゴリID
     * @return カテゴリ。存在しない場合はnull
     */
    public ShopCategoryDto findCategoryById(String categoryId) {
        return shop.getCategories().get(categoryId);
    }

    /**
     * 表示スロットからSHOPカテゴリを取得する。
     *
     * <p>GUI上の配置とカテゴリ定義の対応はRepositoryが把握する。
     * Facadeは「探し方」ではなく「見つかった後の画面遷移」に集中させる。</p>
     *
     * @param slot GUIスロット番号
     * @return カテゴリ。存在しない場合はnull
     */
    public ShopCategoryDto findCategoryBySlot(int slot) {
        return shop.getCategories()
                .values()
                .stream()
                .filter(category -> category.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    /**
     * カテゴリIDと表示スロットからSHOP商品を取得する。
     *
     * <p>商品探索処理をRepositoryに集約し、FacadeがDTOのMap構造へ依存しないようにする。</p>
     *
     * @param categoryId カテゴリID
     * @param slot       GUIスロット番号
     * @return 商品。存在しない場合はnull
     */
    public ShopItemDto findItemBySlot(String categoryId, int slot) {
        ShopCategoryDto category = findCategoryById(categoryId);

        if (category == null) {
            return null;
        }

        return category.getItems()
                .values()
                .stream()
                .filter(item -> item.getSlot() == slot)
                .findFirst()
                .orElse(null);
    }

    /**
     * Materialから売却可能なSHOP商品を取得する。
     *
     * <p>売却対象の検索はSHOP定義全体を横断するため、
     * ServiceではなくRepositoryに閉じ込める。</p>
     *
     * @param material Bukkit Material
     * @return 売却可能商品。存在しない場合はnull
     */
    public ShopItemDto findSellableItem(Material material) {
        return shop.getCategories()
                .values()
                .stream()
                .flatMap(category -> category.getItems().values().stream())
                .filter(ShopItemDto::isSellable)
                .filter(item -> item.getMaterial() == material)
                .findFirst()
                .orElse(null);
    }

    public ShopDto getShop() {
        return shop;
    }
}