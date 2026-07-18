package com.example.rpg.item.repository;

import com.example.rpg.common.exception.*;
import com.example.rpg.item.dto.ItemAttributeDto;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * RPG プラグイン独自の属性情報用Repository
 */
public class YamlAttributeRepository implements IAttributeRepository {
    /**
     * 属性定義のルートセクション名
     */
    private static final String ATTRIBUTES_SECTION_PATH = "attributes";

    /**
     * 属性定義ファイル。
     */
    private final File configurationFile;

    /**
     * 読み込み済み属性定義
     *
     * <p>
     * ListはRepository内部でのみ使用する。
     * </p>
     */
    private final Map<String, ItemAttributeDto> attributes =
            new LinkedHashMap<>();

    /**
     * YAML形式のItemAttributeRepositoryを生成する。
     *
     * @param configurationFile attribute.yml
     * @throws NullPointerException configがnullの場合
     */
    public YamlAttributeRepository(final File configurationFile) {
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
     * ディスク上のattributes.ymlを読み直し、全定義の解析成功後に
     * Repositoryのキャッシュを更新する。
     * </p>
     */
    @Override
    public void load() {
        final YamlConfiguration loadedConfiguration =
                YamlConfiguration.loadConfiguration(
                        configurationFile
                );

        final Map<String, ItemAttributeDto> loadedAttributes =
                loadAttributes(loadedConfiguration);

        attributes.clear();
        attributes.putAll(loadedAttributes);
    }

    /**
     * YAML設定からAttribute定義一覧を読み込む。
     *
     * @param configuration attributes.ymlの読込結果
     * @return Attribute定義一覧
     */
    private Map<String, ItemAttributeDto> loadAttributes(
            final YamlConfiguration configuration
    ) {
        final ConfigurationSection attributesSection =
                configuration.getConfigurationSection(
                        ATTRIBUTES_SECTION_PATH
                );

        if (attributesSection == null) {
            return Map.of();
        }

        final Map<String, ItemAttributeDto> loadedAttributes = new LinkedHashMap<>();

        for (String attributeId : attributesSection.getKeys(false)) {
            final ConfigurationSection attributeSection =
                    attributesSection.getConfigurationSection(
                            attributeId
                    );

            if (attributeSection == null) {
                throw new ConfigurationException(
                        "Attribute定義がセクションではありません。"
                                + " / attributeId="
                                + attributeId
                );
            }

            loadedAttributes.put(
                    attributeId,
                    loadAttribute(
                            attributeId,
                            attributeSection
                    )
            );
        }

        return loadedAttributes;
    }

    /**
     * Attributeを読み込む。
     *
     * @param attributeId 属性ID
     * @param section     Attribute設定
     * @return AttributeModifier情報
     */
    private ItemAttributeDto loadAttribute(
            final String attributeId,
            final ConfigurationSection section
    ) {
        final String attributeName = requireString(section, attributeId, "attribute");
        final Attribute attribute = parseAttribute(attributeId, attributeName);
        final double amount = requireDouble(section, attributeId, "amount");
        final String operationName = requireString(section, attributeId, "operation");
        final AttributeModifier.Operation operation =
                parseOperation(attributeId, operationName);
        final String slotGroupName = requireString(section, attributeId, "slot");
        final EquipmentSlotGroup slotGroup = parseSlotGroup(
                attributeId, slotGroupName);

        return new ItemAttributeDto(
                attributeId,
                attribute,
                amount,
                operation,
                slotGroup
        );
    }

    /**
     * Attribute名をBukkitのAttributeへ変換する。
     *
     * @param attributeId   属性ID
     * @param attributeName Attribute名
     * @return 変換したAttribute
     * @throws InvalidPropertyValueException      Attribute名をNamespacedKeyへ変換できない場合
     * @throws UnknownConfigurationValueException 対応するAttributeが存在しない場合
     */
    private Attribute parseAttribute(
            final String attributeId,
            final String attributeName
    ) {
        final String normalizedName =
                attributeName.toLowerCase(Locale.ROOT);

        final NamespacedKey attributeKey =
                NamespacedKey.fromString(normalizedName);

        if (attributeKey == null) {
            throw new InvalidPropertyValueException(
                    attributeId,
                    "attributes." + attributeId + ".attribute",
                    attributeName,
                    "must be a valid namespaced key"
            );
        }

        final Registry<Attribute> attributeRegistry =
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ATTRIBUTE);

        final Attribute attribute =
                attributeRegistry.get(attributeKey);

        if (attribute == null) {
            throw new UnknownConfigurationValueException(
                    attributeId,
                    "attributes." + attributeId + ".attribute",
                    attributeName
            );
        }

        return attribute;
    }

    /**
     * Operation名をAttributeModifier.Operationへ変換する。
     *
     * @param attributeId   属性ID
     * @param operationName Operation名
     * @return AttributeModifier.Operation
     * @throws InvalidEnumValueException Operationへ変換できない場合
     */
    private AttributeModifier.Operation parseOperation(
            final String attributeId,
            final String operationName
    ) {
        try {
            return AttributeModifier.Operation.valueOf(
                    operationName.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidEnumValueException(
                    attributeId,
                    "attributes." + attributeId + ".operation",
                    operationName,
                    AttributeModifier.Operation.class
            );
        }
    }

    /**
     * EquipmentSlotGroup名をBukkitのEquipmentSlotGroupへ変換する。
     *
     * @param attributeId   属性ID
     * @param slotGroupName EquipmentSlotGroup名
     * @return EquipmentSlotGroup
     * @throws UnknownConfigurationValueException 存在しないSlotGroupの場合
     */
    private EquipmentSlotGroup parseSlotGroup(
            final String attributeId,
            final String slotGroupName
    ) {
        final EquipmentSlotGroup slotGroup =
                EquipmentSlotGroup.getByName(
                        slotGroupName.toLowerCase(Locale.ROOT)
                );

        if (slotGroup == null) {
            throw new UnknownConfigurationValueException(
                    attributeId,
                    "attributes." + attributeId + ".slot",
                    slotGroupName
            );
        }

        return slotGroup;
    }

    /**
     * 必須文字列を取得する。
     *
     * @param section      読み込み対象セクション
     * @param attributeId  属性
     * @param attributeKey キー
     * @return 空ではない文字列
     */
    private String requireString(
            final ConfigurationSection section,
            final String attributeId,
            final String attributeKey
    ) {
        final String propertyPath =
                ATTRIBUTES_SECTION_PATH + "." + attributeId;

        if (!section.isSet(attributeKey)) {
            throw new RequiredPropertyException(
                    attributeId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(attributeKey);

        if (!(rawValue instanceof String value)) {
            throw new InvalidPropertyTypeException(
                    attributeKey,
                    propertyPath,
                    "string",
                    rawValue
            );
        }

        if (value.isBlank()) {
            throw new InvalidPropertyValueException(
                    attributeId,
                    propertyPath,
                    value,
                    "must not be blank"
            );
        }

        return value;
    }

    /**
     * 必須の有限な数値を取得する。
     *
     * @param section      読み込み対象セクション
     * @param attributeId  属性ID
     * @param attributeKey キー名
     * @return 有限なdouble値
     */
    private double requireDouble(
            final ConfigurationSection section,
            final String attributeId,
            final String attributeKey
    ) {
        final String propertyPath =
                ATTRIBUTES_SECTION_PATH + "." + attributeId + "." + attributeKey;

        if (!section.isSet(attributeKey)) {
            throw new RequiredPropertyException(
                    attributeId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(attributeKey);

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    attributeId,
                    propertyPath,
                    "number",
                    rawValue
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)) {
            throw new InvalidPropertyValueException(
                    attributeId,
                    propertyPath,
                    rawValue,
                    "must be a finite number"
            );
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemAttributeDto findById(final String attributeId) {
        return attributes.get(attributeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ItemAttributeDto> findAll() {
        return attributes;
    }
}
