package com.example.rpg.shop.repository;

import com.example.rpg.common.exception.ConfigurationException;
import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.shop.dto.ShopCategoryDto;
import com.example.rpg.shop.dto.ShopDto;
import com.example.rpg.shop.dto.ShopItemDto;
import com.example.rpg.shop.dto.ShopItemType;
import com.example.rpg.shop.repository.interfaces.IShopRepository;
import com.example.rpg.util.RpgUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

/**
 * SHOP定義を管理するRepository
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
public class YamlShopRepository extends AbstractYamlRepository<ShopDto> implements IShopRepository {

    /**
     * SHOP定義のルートセクション名
     */
    private static final String SHOP_SECTION_PATH = "shop";

    /**
     * SHOP Repositoryを生成する。
     *
     * @param configurationFile shop.yml
     */
    public YamlShopRepository(File configurationFile) {
        super(configurationFile);

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
                    "shop.yml に shop セクションがありません。"
            );
        }

        final String title = shopSection.getString(
                "title",
                "<gold>SHOP</gold>"
        );

        final int size = loadInventorySize(shopSection);

        final Map<String, ShopCategoryDto> categories =
                loadCategories(shopSection);

        return new ShopDto(
                title,
                size,
                categories
        );
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
        final Object rawValue =
                shopSection.get("size", 27);

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    "shop",
                    "shop.size",
                    "integer",
                    rawValue
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)
                || value != Math.rint(value)) {
            throw new InvalidPropertyValueException(
                    "shop",
                    "shop.size",
                    rawValue,
                    "must be a finite integer"
            );
        }

        final int size = (int) value;

        /*
         * Bukkit InventoryのCHEST形式は9の倍数で、
         * 通常使用可能な範囲は9～54。
         */
        if (size < 9
                || size > 54
                || size % 9 != 0) {
            throw new InvalidPropertyValueException(
                    "shop",
                    "shop.size",
                    size,
                    "must be a multiple of 9 between 9 and 54"
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
        final ConfigurationSection categoriesSection =
                shopSection.getConfigurationSection(
                        "categories"
                );

        if (categoriesSection == null) {
            return Map.of();
        }

        final Map<String, ShopCategoryDto> categories =
                new LinkedHashMap<>();

        for (String categoryId
                : categoriesSection.getKeys(false)) {

            final ConfigurationSection categorySection =
                    categoriesSection.getConfigurationSection(
                            categoryId
                    );

            if (categorySection == null) {
                throw new InvalidPropertyTypeException(
                        categoryId,
                        "shop.categories." + categoryId,
                        "section",
                        categoriesSection.get(categoryId)
                );
            }

            categories.put(
                    categoryId,
                    loadCategory(
                            categoryId,
                            categorySection
                    )
            );
        }

        return categories;
    }

    /**
     * カテゴリ定義を読み込む。
     *
     * @param categoryId カテゴリID
     * @param section    カテゴリ設定
     * @return カテゴリ定義
     */
    private ShopCategoryDto loadCategory(
            final String categoryId,
            final ConfigurationSection section
    ) {
        final String name = section.getString(
                "name",
                categoryId
        );

        final int slot = RpgUtil.getIntOrDefault(
                section,
                "slot",
                0
        );

        final String iconName = section.getString(
                "icon",
                "CHEST"
        );

        final Material icon =
                Material.matchMaterial(iconName);

        if (icon == null) {
            throw new InvalidPropertyValueException(
                    categoryId,
                    "shop.categories."
                            + categoryId
                            + ".icon",
                    iconName
            );
        }

        final Map<String, ShopItemDto> items =
                loadItems(
                        categoryId,
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
     * @param categoryId カテゴリID
     * @param section    カテゴリ設定
     * @return 商品定義
     */
    private Map<String, ShopItemDto> loadItems(
            final String categoryId,
            final ConfigurationSection section
    ) {
        final ConfigurationSection itemsSection =
                section.getConfigurationSection("items");

        if (itemsSection == null) {
            return Map.of();
        }

        final Map<String, ShopItemDto> items =
                new LinkedHashMap<>();

        for (String shopItemId
                : itemsSection.getKeys(false)) {

            final ConfigurationSection itemSection =
                    itemsSection.getConfigurationSection(
                            shopItemId
                    );

            if (itemSection == null) {
                throw new InvalidPropertyTypeException(
                        shopItemId,
                        "shop.categories."
                                + categoryId
                                + ".items."
                                + shopItemId,
                        "section",
                        itemsSection.get(shopItemId)
                );
            }

            items.put(
                    shopItemId,
                    loadItem(
                            categoryId,
                            shopItemId,
                            itemSection
                    )
            );
        }

        return items;
    }

    /**
     * SHOP商品定義を読み込む。
     *
     * @param categoryId カテゴリID
     * @param shopItemId SHOP商品ID
     * @param section    商品設定
     * @return SHOP商品定義
     */
    private ShopItemDto loadItem(
            final String categoryId,
            final String shopItemId,
            final ConfigurationSection section
    ) {
        final int slot = RpgUtil.getIntOrDefault(
                section,
                "slot",
                0
        );

        final ShopItemType type =
                parseShopItemType(
                        categoryId,
                        shopItemId,
                        section.getString(
                                "type",
                                "ITEM"
                        )
                );

        final String itemId = section.getString(
                "itemId",
                shopItemId
        );

        final int price = RpgUtil.getIntOrDefault(
                section,
                "price",
                0
        );

        /*
         * 売却額が未指定の場合、購入価格の約1/3を設定する。
         */
        final int defaultSellPrice =
                price == 0
                        ? 0
                        : Math.round((float) price / 3);

        final int sellPrice =
                RpgUtil.getIntOrDefault(
                        section,
                        "sellPrice",
                        defaultSellPrice
                );

        final int amount = RpgUtil.getIntOrDefault(
                section,
                "amount",
                1
        );

        final int limit = RpgUtil.getIntOrDefault(
                section,
                "limit",
                -1
        );

        final String permission = section.getString(
                "permission",
                ""
        );

        final List<String> commands =
                section.getStringList("commands");

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
     * SHOP商品種別を解析する。
     *
     * @param categoryId カテゴリID
     * @param shopItemId SHOP商品ID
     * @param typeName   商品種別名
     * @return SHOP商品種別
     */
    private ShopItemType parseShopItemType(
            final String categoryId,
            final String shopItemId,
            final String typeName
    ) {
        try {
            return ShopItemType.valueOf(
                    typeName.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidPropertyValueException(
                    shopItemId,
                    "shop.categories."
                            + categoryId
                            + ".items."
                            + shopItemId
                            + ".type",
                    typeName
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ShopCategoryDto> findCategories() {
        return List.copyOf(
                new ArrayList<>(
                        getCurrentData().getCategories().values()
                )
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
            return Collections.emptyList();
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