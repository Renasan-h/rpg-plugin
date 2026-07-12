package com.example.rpg.repository;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import com.example.rpg.dto.ShopItemType;
import com.example.rpg.repository.interfaces.IShopRepository;
import com.example.rpg.util.RpgUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

/**
 * SHOP定義を管理するRepository。
 *
 * <p>
 * config.ymlからSHOP定義を読み込み、DTOへ変換して保持する。
 * また、カテゴリや商品の検索機能を提供することで、
 * 呼び出し側がDTOの内部構造（MapやListなど）へ依存しないよう責務を集約する。
 * </p>
 *
 * <p>
 * 将来的に設定ファイルからPostgreSQLなどの永続ストレージへ移行した場合でも、
 * ServiceやFacadeはRepository経由で取得するだけとなり、
 * データ取得方法の変更をRepository内へ閉じ込められる。
 * </p>
 */
public class ShopRepository implements IShopRepository {

    private final YamlConfiguration config;
    /**
     * 読み込み済みのSHOP定義。
     *
     * <p>
     * config.ymlの内容をDTOへ変換した結果を保持する。
     * Repository内のみが保持し、外部から直接変更しないことを前提とする。
     * </p>
     */
    private ShopDto shopDto;

    /**
     * SHOP Repositoryを生成する。
     *
     */
    public ShopRepository(YamlConfiguration config) {
        this.config = config;
        load();
    }

    /**
     * {@inheritDoc}
     */
    public void load() {
        ConfigurationSection shopSection = config.getConfigurationSection("shop");

        if (shopSection == null) {
            throw new IllegalStateException("shop.yml に shop がありません");
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
            }
        }

        this.shopDto = new ShopDto(title, size, categories);
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
     * {@inheritDoc}
     */
    public List<ShopCategoryDto> findCategories() {
        return List.copyOf(new ArrayList<>(shopDto.getCategories().values()));
    }

    /**
     * {@inheritDoc}
     */
    public ShopCategoryDto findShopCategoryById(String categoryId) {
        return shopDto.getCategories().get(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    public List<ShopItemDto> findShopItems(String categoryId) {
        ShopCategoryDto category = findShopCategoryById(categoryId);

        if (category == null) {
            return Collections.emptyList();
        }

        return List.copyOf(category.getItems().values());
    }

    /**
     * {@inheritDoc}
     */
    public ShopItemDto findShopItemById(String itemId) {
        Map<String, ShopCategoryDto> categories = shopDto.getCategories();

        if (categories.isEmpty()) {
            return null;
        }

        for (ShopCategoryDto category : categories.values()) {
            for (ShopItemDto item : category.getItems().values()) {
                if (item.getId().equals(itemId)) {
                    return item;
                }
            }

        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    public ShopItemDto findShopSellableItem(Material material) {
        return shopDto.getCategories()
                .values()
                .stream()
                .flatMap(category -> category.getItems().values().stream())
                .filter(ShopItemDto::isSellable)
                .filter(item -> item.getMaterial() == material)
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    public ShopDto getShopDto() {
        return this.shopDto;
    }
}