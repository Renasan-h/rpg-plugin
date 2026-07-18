package com.example.rpg.item.repository;

import com.example.rpg.common.config.ConfigurationValueReader;
import com.example.rpg.common.exception.InvalidEnumValueException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
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

/**
 * RPG プラグイン独自の属性情報用Repository
 *
 * <p>
 * yaml structure</br>
 * attributes:</br>
 * 　　attributeId:</br>
 * 　　　　attribute: {@link String}</br>
 * 　　　　amount: {@link Integer}</br>
 * 　　　　operation: {@link AttributeModifier.Operation}</br>
 * 　　　　slot: {@link EquipmentSlotGroup}</br>
 * </p>
 */
public class YamlAttributeRepository extends AbstractYamlRepository<Map<String, ItemAttributeDto>> implements IAttributeRepository {
    /**
     * 属性定義のルートセクション名
     */
    private static final String ATTRIBUTES_SECTION_PATH = "attributes";

    /**
     * SHOP Repositoryを生成する。
     *
     * @param configurationFile attributes.yml
     */
    public YamlAttributeRepository(final File configurationFile) {
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
     * YAML設定からAttribute定義一覧を読み込む。
     *
     * @param configuration attributes.ymlの読込結果
     * @return Attribute定義一覧
     */
    @Override
    protected Map<String, ItemAttributeDto> parse(
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
            final String propertyRoot =
                    ATTRIBUTES_SECTION_PATH
                            + "."
                            + attributeId;

            final ConfigurationSection attributeSection =
                    ConfigurationValueReader.requireSection(
                            attributesSection,
                            attributeId,
                            attributeId,
                            propertyRoot
                    );

            loadedAttributes.put(
                    attributeId,
                    loadAttribute(
                            attributeId,
                            attributeSection,
                            propertyRoot
                    )
            );
        }

        return Map.copyOf(loadedAttributes);
    }

    /**
     * Attributeを読み込む。
     *
     * @param attributeId  属性ID
     * @param section      Attribute設定
     * @param propertyRoot 定義の完全パス
     * @return AttributeModifier情報
     */
    private ItemAttributeDto loadAttribute(
            final String attributeId,
            final ConfigurationSection section,
            final String propertyRoot
    ) {
        final String attributeName = ConfigurationValueReader.requireString(
                section,
                "attribute",
                attributeId,
                propertyRoot
        );

        final Attribute attribute = parseAttribute(attributeId, attributeName);

        final double amount = ConfigurationValueReader.requireDouble(
                section,
                "amount",
                attributeId,
                propertyRoot + ".amount"
        );

        final String operationName = ConfigurationValueReader.requireString(
                section,
                "operation",
                attributeId,
                propertyRoot + ".operation"
        );

        final AttributeModifier.Operation operation =
                parseOperation(attributeId, operationName);

        final String slotGroupName = ConfigurationValueReader.requireString(
                section,
                "slot",
                attributeId,
                propertyRoot + ".slot"
        );

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
        final String propertyPath =
                ATTRIBUTES_SECTION_PATH
                        + "."
                        + attributeId
                        + ".attribute";

        final NamespacedKey attributeKey =
                NamespacedKey.fromString(
                        attributeName.toLowerCase(Locale.ROOT)
                );

        if (attributeKey == null) {
            throw new InvalidPropertyValueException(
                    attributeId,
                    propertyPath,
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
                    propertyPath + ".attribute",
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
        final String propertyPath =
                ATTRIBUTES_SECTION_PATH
                        + "."
                        + attributeId
                        + ".operation";

        try {
            return AttributeModifier.Operation.valueOf(
                    operationName.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new InvalidEnumValueException(
                    attributeId,
                    propertyPath,
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
        final String propertyPath =
                ATTRIBUTES_SECTION_PATH
                        + "."
                        + attributeId
                        + ".slot";

        final EquipmentSlotGroup slotGroup =
                EquipmentSlotGroup.getByName(
                        slotGroupName.toLowerCase(Locale.ROOT)
                );

        if (slotGroup == null) {
            throw new UnknownConfigurationValueException(
                    attributeId,
                    propertyPath,
                    slotGroupName
            );
        }

        return slotGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemAttributeDto findById(final String attributeId) {
        return getCurrentData().get(attributeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ItemAttributeDto> findAll() {
        return getCurrentData();
    }
}
