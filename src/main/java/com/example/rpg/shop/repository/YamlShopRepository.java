package com.example.rpg.shop.repository;

import com.example.rpg.common.config.ConfigurationValueReader;
import com.example.rpg.common.exception.*;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.shop.dto.ShopCategoryDto;
import com.example.rpg.shop.dto.ShopDto;
import com.example.rpg.shop.dto.ShopItemDto;
import com.example.rpg.shop.dto.ShopItemType;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import com.example.rpg.shop.validator.ShopDefinitionValidator;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * SHOP定義を管理するRepository
 *
 * <p>
 * yaml structure</br>
 * shop:</br>
 * 　　title: {@link String} Category list title</br>
 * 　　size: {@link Integer} 9 ~ 54</br>
 * 　　categories:</br>
 * 　　　categoryName:</br>
 * 　　　　slot: {@link Integer}</br>
 * 　　　　icon: {@link Material}</br>
 * 　　　　name: {@link String} display Name on Category list</br>
 * 　　　　items:</br>
 * 　　　　　　itemId:</br>
 * 　　　　　　　　itemId: {@link String}</br>
 * 　　　　　　　　slot: {@link Integer}</br>
 * 　　　　　　　　price: {@link Integer}</br>
 * 　　　　　　　　sellPrice: {@link Integer}</br>
 * 　　　　　　　　amount: {@link Integer}</br>
 * </p>
 */
public final class YamlShopRepository extends AbstractYamlRepository<ShopDto> implements IShopRepository {

    /**
     * SHOP定義のルートセクション名
     */
    private static final String SHOP_SECTION_PATH = "shop";

    /**
     * SHOP定義Validator
     */
    private final ShopDefinitionValidator validator;

    /**
     * YAML形式のSHOP Repositoryを生成する。
     *
     * @param configurationFile shop.yml
     * @param validator         SHOP定義Validator
     * @throws NullPointerException 引数がnullの場合
     */
    public YamlShopRepository(
            final File configurationFile,
            final ShopDefinitionValidator validator
    ) {
        super(configurationFile);

        this.validator = Objects.requireNonNull(
                validator,
                "validator must not be null"
        );

        load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        reloadData();
    }

    /**
     * YAML設定からSHOP定義を生成する。
     *
     * @param configuration shop.ymlの読込結果
     * @return SHOP定義
     */
    @Override
    protected ShopDto parse(
            final YamlConfiguration configuration
    ) {
        final ConfigurationSection shopSection =
                configuration.getConfigurationSection(
                        SHOP_SECTION_PATH
                );

        if (shopSection == null) {
            throw new ConfigurationException(
                    getConfigurationFileName()
                            + " に shop セクションがありません。"
            );
        }

        final String title =
                ConfigurationValueReader.getStringOrDefault(
                        shopSection,
                        "title",
                        SHOP_SECTION_PATH,
                        SHOP_SECTION_PATH + ".title",
                        "<gold>SHOP</gold>"
                );

        final int size =
                loadInventorySize(shopSection);

        final Map<String, ShopCategoryDto> categories =
                loadCategories(shopSection);

        return new ShopDto(
                title,
                size,
                categories
        );
    }

    /**
     * 読み込んだShop定義を検証する。
     *
     * @param candidateData 検証対象のShop定義
     */
    @Override
    protected void validate(final ShopDto candidateData) {
        validator.validate(candidateData);
    }

    /**
     * SHOPインベントリサイズを読み込む。
     *
     * @param shopSection shopセクション
     * @return 検証済みインベントリサイズ
     */
    private int loadInventorySize(
            final ConfigurationSection shopSection
    ) {
        final int size =
                ConfigurationValueReader.getIntOrDefault(
                        shopSection,
                        "size",
                        SHOP_SECTION_PATH,
                        SHOP_SECTION_PATH + ".size",
                        27,
                        9,
                        54
                );

        /*
         * BukkitのCHEST形式Inventoryは、
         * 1行9スロット単位で構成される。
         */
        if (size % 9 != 0) {
            throw new InvalidPropertyValueException(
                    SHOP_SECTION_PATH,
                    SHOP_SECTION_PATH + ".size",
                    size,
                    "must be a multiple of 9"
            );
        }

        return size;
    }

    /**
     * SHOPカテゴリ一覧を読み込む。
     *
     * @param shopSection shopセクション
     * @return カテゴリ定義
     */
    private Map<String, ShopCategoryDto> loadCategories(
            final ConfigurationSection shopSection
    ) {
        if (!shopSection.isSet("categories")) {
            return Map.of();
        }

        final ConfigurationSection categoriesSection =
                ConfigurationValueReader.requireSection(
                        shopSection,
                        "categories",
                        SHOP_SECTION_PATH,
                        SHOP_SECTION_PATH + ".categories"
                );

        final Map<String, ShopCategoryDto> categories =
                new LinkedHashMap<>();

        for (String categoryId : categoriesSection.getKeys(false)) {

            final String propertyRoot =
                    SHOP_SECTION_PATH
                            + ".categories."
                            + categoryId;

            final ConfigurationSection categorySection =
                    ConfigurationValueReader.requireSection(
                            categoriesSection,
                            categoryId,
                            categoryId,
                            propertyRoot
                    );

            categories.put(
                    categoryId,
                    loadCategory(
                            categoryId,
                            propertyRoot,
                            categorySection
                    )
            );
        }

        return Map.copyOf(categories);
    }

    /**
     * カテゴリ定義を読み込む。
     *
     * @param categoryId   カテゴリID
     * @param propertyRoot カテゴリ定義の完全パス
     * @param section      カテゴリ設定
     * @return カテゴリ定義
     */
    private ShopCategoryDto loadCategory(
            final String categoryId,
            final String propertyRoot,
            final ConfigurationSection section
    ) {
        final String name =
                ConfigurationValueReader.getStringOrDefault(
                        section,
                        "name",
                        categoryId,
                        propertyRoot + ".name",
                        categoryId
                );

        final int slot =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "slot",
                        categoryId,
                        propertyRoot + ".slot",
                        0,
                        0,
                        Integer.MAX_VALUE
                );

        final String iconName =
                ConfigurationValueReader.getStringOrDefault(
                        section,
                        "icon",
                        categoryId,
                        propertyRoot + ".icon",
                        "CHEST"
                );

        final Material icon =
                parseMaterial(
                        categoryId,
                        propertyRoot + ".icon",
                        iconName
                );

        final Map<String, ShopItemDto> items =
                loadItems(
                        categoryId,
                        propertyRoot,
                        section
                );

        return new ShopCategoryDto(
                categoryId,
                name,
                slot,
                icon,
                items
        );
    }

    /**
     * カテゴリ内の商品一覧を読み込む。
     *
     * @param categoryId   カテゴリID
     * @param propertyRoot カテゴリ定義の完全パス
     * @param section      カテゴリ設定
     * @return 商品定義
     */
    private Map<String, ShopItemDto> loadItems(
            final String categoryId,
            final String propertyRoot,
            final ConfigurationSection section
    ) {
        if (!section.isSet("items")) {
            return Map.of();
        }

        final ConfigurationSection itemsSection =
                ConfigurationValueReader.requireSection(
                        section,
                        "items",
                        categoryId,
                        propertyRoot + ".items"
                );

        final Map<String, ShopItemDto> items =
                new LinkedHashMap<>();

        for (String shopItemId
                : itemsSection.getKeys(false)) {

            final String itemPropertyRoot =
                    propertyRoot
                            + ".items."
                            + shopItemId;

            final ConfigurationSection itemSection =
                    ConfigurationValueReader.requireSection(
                            itemsSection,
                            shopItemId,
                            shopItemId,
                            itemPropertyRoot
                    );

            items.put(
                    shopItemId,
                    loadItem(
                            shopItemId,
                            itemPropertyRoot,
                            itemSection
                    )
            );
        }

        return Map.copyOf(items);
    }

    /**
     * SHOP商品定義を読み込む。
     *
     * @param shopItemId   SHOP商品ID
     * @param propertyRoot 商品定義の完全パス
     * @param section      商品設定
     * @return SHOP商品定義
     */
    private ShopItemDto loadItem(
            final String shopItemId,
            final String propertyRoot,
            final ConfigurationSection section
    ) {
        final int slot =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "slot",
                        shopItemId,
                        propertyRoot + ".slot",
                        0,
                        0,
                        Integer.MAX_VALUE
                );

        final String typeName =
                ConfigurationValueReader.getStringOrDefault(
                        section,
                        "type",
                        shopItemId,
                        propertyRoot + ".type",
                        "ITEM"
                );

        final ShopItemType type =
                parseShopItemType(
                        shopItemId,
                        propertyRoot + ".type",
                        typeName
                );

        final String itemId =
                ConfigurationValueReader.getStringOrDefault(
                        section,
                        "itemId",
                        shopItemId,
                        propertyRoot + ".itemId",
                        shopItemId
                );

        final int price =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "price",
                        shopItemId,
                        propertyRoot + ".price",
                        0,
                        0,
                        Integer.MAX_VALUE
                );

        /*
         * 売却額が未指定の場合、購入価格の約1/3を設定する。
         */
        final int defaultSellPrice =
                calculateDefaultSellPrice(price);

        final int sellPrice =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "sellPrice",
                        shopItemId,
                        propertyRoot + ".sellPrice",
                        defaultSellPrice,
                        0,
                        Integer.MAX_VALUE
                );

        final int amount =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "amount",
                        shopItemId,
                        propertyRoot + ".amount",
                        1,
                        1,
                        Integer.MAX_VALUE
                );

        final int limit =
                ConfigurationValueReader.getIntOrDefault(
                        section,
                        "limit",
                        shopItemId,
                        propertyRoot + ".limit",
                        -1,
                        -1,
                        Integer.MAX_VALUE
                );

        final String permission =
                readPermission(
                        section,
                        shopItemId,
                        propertyRoot
                );

        final List<String> commands =
                ConfigurationValueReader.getStringListOrEmpty(
                        section,
                        "commands",
                        shopItemId,
                        propertyRoot + ".commands"
                );

        return new ShopItemDto(
                shopItemId,
                itemId,
                slot,
                type,
                price,
                sellPrice,
                amount,
                limit,
                permission,
                commands
        );
    }

    /**
     * 購入価格からデフォルト売却価格を算出する。
     *
     * @param price 購入価格
     * @return デフォルト売却価格
     */
    private int calculateDefaultSellPrice(
            final int price
    ) {
        if (price == 0) {
            return 0;
        }

        return Math.round(
                (float) price / 3
        );
    }

    /**
     * 任意の権限ノードを読み込む。
     *
     * <p>
     * 未指定または空文字の場合は、権限制限なしとして空文字を返す。
     * </p>
     *
     * @param section      商品設定
     * @param shopItemId   SHOP商品ID
     * @param propertyRoot 商品定義の完全パス
     * @return 権限ノード
     */
    private String readPermission(
            final ConfigurationSection section,
            final String shopItemId,
            final String propertyRoot
    ) {
        if (!section.isSet("permission")) {
            return "";
        }

        final Object rawValue =
                section.get("permission");

        if (!(rawValue instanceof String permission)) {
            throw new InvalidPropertyTypeException(
                    shopItemId,
                    propertyRoot + ".permission",
                    "string",
                    rawValue
            );
        }

        return permission.trim();
    }

    /**
     * SHOP商品種別を解析する。
     *
     * @param shopItemId   SHOP商品ID
     * @param propertyPath 完全なプロパティパス
     * @param typeName     商品種別名
     * @return SHOP商品種別
     */
    private ShopItemType parseShopItemType(
            final String shopItemId,
            final String propertyPath,
            final String typeName
    ) {
        try {
            return ShopItemType.valueOf(
                    typeName.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidEnumValueException(
                    shopItemId,
                    propertyPath,
                    typeName,
                    ShopItemType.class
            );
        }
    }

    /**
     * Material名をBukkit Materialへ変換する。
     *
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param materialName Material名
     * @return Bukkit Material
     */
    private Material parseMaterial(
            final String definitionId,
            final String propertyPath,
            final String materialName
    ) {
        final Material material =
                Material.matchMaterial(materialName);

        if (material == null) {
            throw new UnknownConfigurationValueException(
                    definitionId,
                    propertyPath,
                    materialName
            );
        }

        return material;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShopCategoryDto> findCategories() {
        return List.copyOf(
                getCurrentData().getCategories().values()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShopCategoryDto findShopCategoryById(
            final String categoryId
    ) {
        return getCurrentData().getCategories().get(categoryId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShopItemDto> findShopItems(
            final String categoryId
    ) {
        final ShopCategoryDto category =
                findShopCategoryById(categoryId);

        if (category == null) {
            return List.of();
        }

        return List.copyOf(
                category.getItems().values()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShopItemDto findShopItemById(
            final String shopItemId
    ) {
        if (shopItemId == null
                || shopItemId.isBlank()) {
            return null;
        }

        return getCurrentData().getCategories()
                .values()
                .stream()
                .flatMap(category ->
                        category.getItems()
                                .values()
                                .stream())
                .filter(item ->
                        item.getId().equals(shopItemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShopItemDto findShopSellableItem(
            final String itemId
    ) {
        if (itemId == null
                || itemId.isBlank()) {
            return null;
        }

        return getCurrentData().getCategories()
                .values()
                .stream()
                .flatMap(category ->
                        category.getItems()
                                .values()
                                .stream())
                .filter(ShopItemDto::isSellable)
                .filter(item ->
                        item.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ShopDto getShop() {
        return getCurrentData();
    }
}