package com.example.rpg.item.repository;


import com.example.rpg.common.exception.ConfigurationException;
import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.item.dto.ItemAttributeDto;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEnchantDto;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;

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
     * items.ymlの読み込み結果
     */
    private final YamlConfiguration config;

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
     * @param config items.ymlの読み込み結果
     * @throws NullPointerException configがnullの場合
     */
    public YamlItemRepository(final YamlConfiguration config) {
        this.config = Objects.requireNonNull(
                config,
                "config must not be null"
        );

        load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        final ConfigurationSection itemsSection =
                config.getConfigurationSection(ITEMS_SECTION_PATH);

        if (itemsSection == null) {
            throw new ConfigurationException(
                    "items.yml に items セクションがありません。"
            );
        }

        // reload時に削除済みの定義が残らないよう、
        // 新しい定義を読み込む前にキャッシュを破棄する
        items.clear();

        for (String itemId : itemsSection.getKeys(false)) {
            final ConfigurationSection itemSection =
                    itemsSection.getConfigurationSection(itemId);

            if (itemSection == null) {
                continue;
            }

            items.put(itemId,
                    loadItem(itemId, itemSection));
        }
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

        final List<ItemEnchantDto> enchantments =
                loadEnchantments(itemId, section);

        final List<ItemAttributeDto> attributes =
                loadAttributes(itemId, section);

        return new ItemDto(
                itemId,
                material,
                displayName,
                lore,
                itemFlags,
                unbreakable,
                customModelData,
                enchantments,
                attributes
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
    private List<ItemEnchantDto> loadEnchantments(
            final String itemId,
            final ConfigurationSection section
    ) {
        final ConfigurationSection enchantmentsSection =
                section.getConfigurationSection("enchantments");

        if (enchantmentsSection == null) {
            return List.of();
        }

        final Registry<Enchantment> enchantmentRegistry =
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT);

        final List<ItemEnchantDto> enchantments = new ArrayList<>();
        final Set<NamespacedKey> loadedKeys = new HashSet<>();

        for (String enchantmentName : enchantmentsSection.getKeys(false)) {
            final ConfigurationSection enchantmentSection =
                    enchantmentsSection.getConfigurationSection(enchantmentName);

            if (enchantmentSection == null) {
                throw new InvalidPropertyTypeException(
                        itemId,
                        "Enchantment",
                        "definition must be a section",
                        enchantmentName

                );
            }

            final NamespacedKey enchantmentKey =
                    parseEnchantmentKey(
                            itemId,
                            enchantmentName
                    );

            if (!loadedKeys.add(enchantmentKey)) {
                throw new InvalidPropertyTypeException(
                        itemId,
                        "enchantment",
                        "Duplicate",
                        enchantmentKey
                );
            }

            final Enchantment enchantment =
                    enchantmentRegistry.get(enchantmentKey);

            if (enchantment == null) {
                throw new UnknownConfigurationValueException(
                        itemId,
                        "enchantment",
                        enchantmentKey.getKey()
                );
            }

            final int level = loadEnchantmentLevel(
                    itemId,
                    enchantmentKey,
                    enchantmentSection
            );

            final boolean ignoreLevelRestriction =
                    enchantmentSection.getBoolean(
                            "ignoreLevelRestriction",
                            false
                    );

            validateEnchantmentLevel(
                    itemId,
                    enchantment,
                    level,
                    ignoreLevelRestriction
            );

            enchantments.add(
                    new ItemEnchantDto(
                            enchantment,
                            level,
                            ignoreLevelRestriction
                    )
            );
        }

        return enchantments;
    }

    /**
     * エンチャント名をNamespacedKeyへ変換する。
     *
     * <p>
     * 名前空間が省略された場合はminecraft名前空間を使用する。
     * </p>
     *
     * @param itemId          アイテムID
     * @param enchantmentName エンチャント名
     * @return NamespacedKey
     */
    private NamespacedKey parseEnchantmentKey(
            final String itemId,
            final String enchantmentName
    ) {
        final String normalizedName =
                enchantmentName.toLowerCase(Locale.ROOT);

        final NamespacedKey key =
                NamespacedKey.fromString(normalizedName);

        if (key == null) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "enchantment key",
                    enchantmentName
            );
        }

        return key;
    }

    /**
     * エンチャントレベルを読み込む。
     *
     * @param itemId             アイテムID
     * @param enchantmentKey     エンチャントキー
     * @param enchantmentSection エンチャント設定
     * @return エンチャントレベル
     */
    private int loadEnchantmentLevel(
            final String itemId,
            final NamespacedKey enchantmentKey,
            final ConfigurationSection enchantmentSection
    ) {
        final Object rawLevel =
                enchantmentSection.get("level");

        if (!(rawLevel instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "Enchantment",
                    enchantmentKey.getKey(),
                    "level must be an integer"
            );
        }

        final double doubleValue = number.doubleValue();

        if (!Double.isFinite(doubleValue)
                || doubleValue != Math.rint(doubleValue)
                || doubleValue < 1
                || doubleValue > Integer.MAX_VALUE
        ) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "enchantments." + enchantmentKey + ".level",
                    rawLevel,
                    "must be greater than or equal to 1"
            );
        }

        return (int) doubleValue;
    }

    /**
     * エンチャントレベルを検証する。
     *
     * @param itemId                 アイテムID
     * @param enchantment            エンチャント
     * @param level                  設定レベル
     * @param ignoreLevelRestriction レベル制限を無視する場合true
     */
    private void validateEnchantmentLevel(
            final String itemId,
            final Enchantment enchantment,
            final int level,
            final boolean ignoreLevelRestriction
    ) {
        if (ignoreLevelRestriction) {
            return;
        }

        if (level < enchantment.getStartLevel()
                || level > enchantment.getMaxLevel()) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "enchantments." + enchantment.getKey() + ".level",
                    level,
                    "must be between 1 and " + enchantment.getMaxLevel()
            );
        }
    }

    /**
     * YAMLからAttributeModifier一覧を読み込む。
     *
     * @param itemId  アイテムID
     * @param section アイテム設定
     * @return AttributeModifier一覧
     */
    private List<ItemAttributeDto> loadAttributes(
            final String itemId,
            final ConfigurationSection section
    ) {
        final ConfigurationSection attributesSection =
                section.getConfigurationSection("attributes");

        if (attributesSection == null) {
            return List.of();
        }

        final List<ItemAttributeDto> attributes = new ArrayList<>();

        for (String key : attributesSection.getKeys(false)) {
            final ConfigurationSection value =
                    attributesSection.getConfigurationSection(key);

            if (value == null) {
                continue;
            }

            attributes.add(loadAttribute(
                    itemId,
                    key,
                    section
            ));

        }

        return attributes;
    }

    /**
     * AttributeModifierを読み込む。
     *
     * @param itemId  アイテムID
     * @param key     Attributeキー
     * @param section Attribute設定
     * @return AttributeModifier情報
     */
    private ItemAttributeDto loadAttribute(
            final String itemId,
            final String key,
            final ConfigurationSection section
    ) {
        final Attribute attribute = parseAttribute(itemId, key);
        final double amount = section.getDouble("amount");
        final AttributeModifier.Operation operation =
                parseOperation(itemId, section.getString("operation"));
        final EquipmentSlotGroup slotGroup = parseSlopGroup(
                itemId, section.getString("slop")
        );

        return new ItemAttributeDto(
                attribute,
                amount,
                operation,
                slotGroup
        );
    }

    /**
     * Attributeを取得する。
     */
    private Attribute parseAttribute(
            final String itemId,
            final String value
    ) {
        final Registry<Attribute> registry =
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);

        final NamespacedKey key =
                NamespacedKey.minecraft(
                        value.toUpperCase(Locale.ROOT)
                );

        final Attribute attribute =
                registry.get(key);

        if (attribute == null) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    "Attribute",
                    value
            );
        }

        return attribute;
    }

    /**
     * Operationを取得する。
     */
    private AttributeModifier.Operation parseOperation(
            final String itemId,
            final String value
    ) {
        try {
            return AttributeModifier.Operation.valueOf(
                    value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    "Operation",
                    value
            );
        }
    }

    /**
     * EquipmentSlotGroupを取得する。
     */
    private EquipmentSlotGroup parseSlopGroup(
            final String itemId,
            final String value
    ) {
        try {
            return EquipmentSlotGroup.getByName(
                    value.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    "slot group",
                    value
            );
        }
    }

    /**
     * 必須文字列を設定セクションから取得する。
     *
     * <p>
     * 未設定値を後続処理へnullとして渡さず、
     * 設定ファイル上の問題として読み込み時に検出する。
     * </p>
     *
     * @param section       読み込み対象セクション
     * @param path          設定キー
     * @param itemId        アイテムID
     * @param attributeName Attribute名
     * @return 空ではない文字列
     * @throws IllegalArgumentException 未設定、空文字、文字列以外の場合
     */
    private String requireString(
            final ConfigurationSection section,
            final String path,
            final String itemId,
            final String attributeName
    ) {
        final Object rawValue = section.get(path);

        if (!(rawValue instanceof String value) || value.isBlank()) {
            throw new IllegalArgumentException(
                    "'" + path + "' must be a non-blank string"
                            + " / attribute=" + attributeName
                            + " / itemId=" + itemId
            );
        }

        return value;
    }

    /**
     * 必須の有限な数値を設定セクションから取得する。
     *
     * <p>
     * ConfigurationSection#getDoubleは未設定や型不正時に
     * 0を返す可能性があるため、元の値を直接検証する。
     * </p>
     *
     * @param section       読み込み対象セクション
     * @param path          設定キー
     * @param itemId        アイテムID
     * @param attributeName Attribute名
     * @return 有限なdouble値
     * @throws IllegalArgumentException 未設定、数値以外、有限値でない場合
     */
    private double requiredDouble(
            final ConfigurationSection section,
            final String path,
            final String itemId,
            final String attributeName
    ) {
        final Object rawValue = section.get(path);

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "attributes." + attributeName + "." + path,
                    "number"
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)) {
            throw new InvalidPropertyTypeException(
                    itemId,
                    "attributes." + attributeName + "." + path,
                    "Infinite"
            );
        }

        return value;
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
