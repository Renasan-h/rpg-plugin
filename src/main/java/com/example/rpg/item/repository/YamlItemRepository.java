package com.example.rpg.item.repository;


import com.example.rpg.common.exception.ConfigurationException;
import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.util.*;

/**
 * items.ymlからRPGアイテム定義を読み込むRepository
 *
 * <p>
 * YAMLの構造解析とDTOへの変換を担当する。
 * 読み込んだアイテムはRepository内部のMapへ保持し、
 * 呼び出し側へMapを公開しない。
 * </p>
 */
public class YamlItemRepository implements IItemRepository {
    /**
     * アイテム定義のルートセクション名
     */
    private static final String ITEMS_SECTION_PATH = "items";

    /**
     * アイテム定義ファイル。
     */
    private final File configurationFile;

    /**
     * 読み込み済みアイテム定義
     *
     * <p>
     * キーはアイテムID、値はアイテム定義とする。
     * MapはRepository内部でのみ使用する。
     * </p>
     */
    private final Map<String, ItemDto> items =
            new LinkedHashMap<>();

    /**
     * YAML形式のItemRepositoryを生成する。
     *
     * @param configurationFile items.yml
     * @throws NullPointerException configurationFileがnullの場合
     */
    public YamlItemRepository(final File configurationFile) {
        this.configurationFile = Objects.requireNonNull(
                configurationFile,
                "configurationFile must not be null"
        );

        load();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * ディスク上のitems.ymlを読み直し、すべての定義を正常に
     * 解析できた場合のみ、現在のキャッシュを更新する。
     * </p>
     */
    @Override
    public void load() {
        final YamlConfiguration loadedConfiguration =
                YamlConfiguration.loadConfiguration(
                        configurationFile
                );

        final Map<String, ItemDto> loadedItems =
                loadItems(loadedConfiguration);

        items.clear();
        items.putAll(loadedItems);
    }

    /**
     * YAML設定からアイテム定義一覧を読み込む。
     *
     * <p>
     * このメソッドでは現在のRepositoryキャッシュを変更しない。
     * 全定義を正常に解析できた場合のみ、呼び出し元がキャッシュを
     * 更新する。
     * </p>
     *
     * @param configuration items.ymlの読込結果
     * @return 読み込んだアイテム定義
     * @throws ConfigurationException itemsセクションが存在しない場合
     */
    private Map<String, ItemDto> loadItems(
            final YamlConfiguration configuration
    ) {
        final ConfigurationSection itemsSection =
                configuration.getConfigurationSection(
                        ITEMS_SECTION_PATH
                );

        if (itemsSection == null) {
            throw new ConfigurationException(
                    "items.yml に items セクションがありません。"
            );
        }

        final Map<String, ItemDto> loadedItems = new LinkedHashMap<>();

        for (String itemId : itemsSection.getKeys(false)) {
            final ConfigurationSection itemSection =
                    itemsSection.getConfigurationSection(itemId);

            if (itemSection == null) {
                throw new ConfigurationException(
                        "アイテム定義がセクションではありません。"
                                + " / itemId="
                                + itemId
                );
            }

            loadedItems.put(
                    itemId,
                    loadItem(itemId, itemSection)
            );
        }

        return loadedItems;
    }

    /**
     * YAMLセクションからアイテム定義を生成する。
     *
     * @param itemId  ItemId
     * @param section アイテム定義セクション
     * @return 生成したItemDto
     * @throws UnknownConfigurationValueException レジストリ値不正
     */
    private ItemDto loadItem(
            final String itemId,
            final ConfigurationSection section
    ) {
        final String materialName = section.getString("material", "STONE");

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    "Material",
                    materialName
            );
        }

        final String displayName = section.getString("displayName", "<white>" + itemId + "</white>");

        final List<String> lore = section.getStringList("lore");

        final List<ItemFlag> itemFlags = loadItemFlags(
                itemId,
                section
        );

        final boolean unbreakable = section.getBoolean(
                "unbreakable",
                false
        );

        final Integer customModelData = loadCustomModelData(
                itemId,
                section
        );

        final List<String> enchantments =
                loadEnchantments(itemId, section);

        final List<String> attributes =
                loadAttributes(itemId, section);

        final List<String> effects =
                loadEffects(itemId, section);

        return new ItemDto(
                itemId,
                material,
                displayName,
                lore,
                itemFlags,
                unbreakable,
                customModelData,
                enchantments,
                attributes,
                effects
        );
    }

    /**
     * YAMLからItemFlag一覧を読み込む
     *
     * <p>
     * 文字列からBukkitのItemFlagへの変換はRepository内で行い、
     * ItemBuilderへ設定ファイルの形式を漏らさない。
     * </p>
     *
     * @param itemId  アイテムID
     * @param section アイテム設定セクション
     * @return ItemFlag一覧
     * @throws IllegalArgumentException 未対応のItemFlagが指定された場合
     */
    private List<ItemFlag> loadItemFlags(
            final String itemId,
            final ConfigurationSection section
    ) {
        return section.getStringList("itemFlags")
                .stream()
                .map(flaName -> parseItemFlag(itemId, flaName))
                .toList();
    }

    /**
     * ItemFlag名をBukkitのItemFlagへ変換する。
     *
     * @param itemId   アイテムID
     * @param flagName ItemFlag名
     * @return 変換したItemFlag
     * @throws UnknownConfigurationValueException 未対応のItemFlagが指定された場合
     */
    private ItemFlag parseItemFlag(
            final String itemId,
            final String flagName
    ) {
        try {
            return ItemFlag.valueOf(flagName.toUpperCase(Locale.ROOT));
        } catch (UnknownConfigurationValueException ex) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    "ItemFlag",
                    flagName
            );
        }
    }

    /**
     * YAMLからCustomModelDataを読み込む。
     *
     * <p>
     * Paper 1.21.5以降では従来の整数CustomModelDataが、
     * CustomModelDataComponent内の単一floatとして扱われる。
     * </p>
     *
     * @param itemId  アイテムID
     * @param section アイテム設定セクション
     * @return CustomModelData 未指定の場合はnull
     * @throws InvalidPropertyTypeException 数値以外または有限値でない場合
     */
    private Integer loadCustomModelData(
            final String itemId,
            final ConfigurationSection section
    ) {
        if (!section.isSet("customModelData")) {
            return null;
        }

        final Object rawValue = section.get("customModelData");

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "item",
                    "customModelData",
                    "integer"
            );
        }

        final double doubleValue = number.doubleValue();

        if (!Double.isFinite(doubleValue)
                || doubleValue != Math.rint(doubleValue)) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "item",
                    "customModelData",
                    "Infinite"
            );
        }

        if (doubleValue < 0 || doubleValue > Integer.MAX_VALUE) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "customModelData",
                    "is out of range",
                    doubleValue
            );
        }

        return (int) doubleValue;
    }

    /**
     * YAMLからエンチャント一覧を読み込む。
     *
     * @param itemId  アイテムID
     * @param section アイテム設定セクション
     * @return エンチャント一覧
     * @throws IllegalArgumentException 設定値が不正な場合
     */
    private List<String> loadEnchantments(
            final String itemId,
            final ConfigurationSection section
    ) {
        final List<String> enchantments =
                section.getStringList("enchantments");

        if (enchantments.isEmpty()) {
            return List.of();
        }

        return enchantments;
    }

    /**
     * YAMLからAttributeID一覧を読み込む。
     *
     * @param itemId  アイテムID
     * @param section アイテム設定
     * @return AttributeModifier一覧
     */
    private List<String> loadAttributes(
            final String itemId,
            final ConfigurationSection section
    ) {
        final List<String> attributes =
                section.getStringList("attributes");

        if (attributes.isEmpty()) {
            return List.of();
        }

        return attributes;
    }

    /**
     * YAMLからPotionEffect一覧を読み込む。
     *
     * @param itemId  アイテムID
     * @param section アイテム設定
     * @return PotionEffect一覧
     */
    private List<String> loadEffects(
            final String itemId,
            final ConfigurationSection section
    ) {
        final List<String> effects =
                section.getStringList("effects");

        if (effects.isEmpty()) {
            return List.of();
        }

        return effects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemDto findById(final String itemId) {
        return items.get(itemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemDto> findAll() {
        return List.copyOf(
                new ArrayList<>(items.values())
        );
    }
}
