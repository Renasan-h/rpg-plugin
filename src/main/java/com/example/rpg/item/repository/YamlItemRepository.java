package com.example.rpg.item.repository;


import com.example.rpg.common.config.ConfigurationValueReader;
import com.example.rpg.common.exception.ConfigurationException;
import com.example.rpg.common.exception.InvalidEnumValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import com.example.rpg.item.validator.ItemDefinitionValidator;
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
 * yaml structure</br>
 * items:</br>
 * 　　itemId:</br>
 * 　　　　material: {@link Material}</br>
 * 　　　　displayName: {@link String} display item name</br>
 * 　　　　lore:</br>
 * 　　　　　　- {@link String} item description</br>
 * 　　　　itemFlags:</br>
 * 　　　　　　- {@link ItemFlag}</br>
 * 　　　　unbreakable: {@link Boolean}</br>
 * 　　　　customModelData: {@link Integer}</br>
 * 　　　　enchantments:</br>
 * 　　　　　　- {@link String} enchantmentId</br>
 * 　　　　attributes:</br>
 * 　　　　　　- {@link String} attributeId</br>
 * </p>
 */
public final class YamlItemRepository extends AbstractYamlRepository<Map<String, ItemDto>> implements IItemRepository {
    /**
     * アイテム定義のルートセクション名
     */
    private static final String ITEMS_SECTION_PATH = "items";

    /**
     * Item定義Validator。
     */
    private final ItemDefinitionValidator validator;

    /**
     * YAML形式のItemRepositoryを生成する。
     *
     * @param configurationFile items.yml
     * @param validator         Item定義Validator
     * @throws NullPointerException 引数がnullの場合
     */
    public YamlItemRepository(
            final File configurationFile,
            final ItemDefinitionValidator validator
    ) {
        super(configurationFile);

        this.validator = Objects.requireNonNull(
                validator,
                "validator must not be null"
        );

        /*
         * Validatorフィールドの初期化完了後に読み込む。
         */
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
     * YAML設定からアイテム定義一覧を読み込む。
     *
     * @param configuration items.ymlの読込結果
     * @return 読み込んだアイテム定義
     */
    @Override
    protected Map<String, ItemDto> parse(
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
            final String propertyRoot =
                    ITEMS_SECTION_PATH
                            + "."
                            + itemId;

            final ConfigurationSection itemSection =
                    ConfigurationValueReader.requireSection(
                            itemsSection,
                            itemId,
                            itemId,
                            propertyRoot
                    );

            loadedItems.put(
                    itemId,
                    loadItem(
                            itemId,
                            propertyRoot,
                            itemSection
                    )
            );
        }

        return Collections.unmodifiableMap(
                new LinkedHashMap<>(loadedItems)
        );
    }

    /**
     * 読み込んだItem定義を検証する。
     *
     * @param candidateData 検証対象のItem定義
     */
    @Override
    protected void validate(
            final Map<String, ItemDto> candidateData
    ) {
        validator.validateAll(candidateData.values());
    }

    /**
     * YAMLセクションからアイテム定義を生成する。
     *
     * @param itemId       ItemId
     * @param propertyRoot 定義の完全パス
     * @param section      アイテム定義セクション
     * @return 生成したItemDto
     * @throws UnknownConfigurationValueException レジストリ値不正
     */
    private ItemDto loadItem(
            final String itemId,
            final String propertyRoot,
            final ConfigurationSection section
    ) {
        final String materialName = ConfigurationValueReader.getStringOrDefault(
                section,
                "material",
                itemId,
                propertyRoot + ".material",
                "STONE");

        final Material material = parseMaterial(
                itemId,
                propertyRoot,
                materialName
        );

        final String displayName = ConfigurationValueReader.getStringOrDefault(
                section,
                "displayName",
                itemId,
                propertyRoot + ".displayName",
                "<white>" + itemId + "</white>"
        );

        final List<String> lore = ConfigurationValueReader.getStringListOrEmpty(
                section,
                "lore",
                itemId,
                propertyRoot + ".lore"
        );

        final List<ItemFlag> itemFlags = loadItemFlags(
                itemId,
                propertyRoot,
                section
        );

        final boolean unbreakable = ConfigurationValueReader.getBooleanOrDefault(
                section,
                "unbreakable",
                itemId,
                propertyRoot + ".unbreakable",
                false
        );

        final Integer customModelData = ConfigurationValueReader.getOptionalInt(
                section,
                "customModelData",
                itemId,
                propertyRoot + ".customModelData",
                0,
                Integer.MAX_VALUE
        );

        final List<String> enchantments =
                ConfigurationValueReader.getStringListOrEmpty(
                        section,
                        "enchantments",
                        itemId,
                        propertyRoot + ".enchantments"
                );

        final List<String> attributes =
                ConfigurationValueReader.getStringListOrEmpty(
                        section,
                        "attributes",
                        itemId,
                        propertyRoot + ".attributes"
                );

        final List<String> effects =
                ConfigurationValueReader.getStringListOrEmpty(
                        section,
                        "effects",
                        itemId,
                        propertyRoot + ".effects"
                );

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
     * Material名をBukkit Materialへ変換する。
     *
     * @param itemId       アイテム定義ID
     * @param propertyRoot 定義の完全パス
     * @param materialName Material名
     * @return Bukkit Material
     */
    private Material parseMaterial(
            final String itemId,
            final String propertyRoot,
            final String materialName
    ) {
        final Material material =
                Material.matchMaterial(materialName);

        if (material == null) {
            throw new UnknownConfigurationValueException(
                    itemId,
                    propertyRoot + ".material",
                    materialName
            );
        }

        return material;
    }

    /**
     * YAMLからItemFlag一覧を読み込む。
     *
     * @param itemId       アイテム定義ID
     * @param propertyRoot 定義の完全パス
     * @param section      アイテム設定
     * @return ItemFlag一覧
     */
    private List<ItemFlag> loadItemFlags(
            final String itemId,
            final String propertyRoot,
            final ConfigurationSection section
    ) {
        final List<String> flagNames =
                ConfigurationValueReader.getStringListOrEmpty(
                        section,
                        "itemFlags",
                        itemId,
                        propertyRoot + ".itemFlags"
                );

        final List<ItemFlag> itemFlags =
                new ArrayList<>(flagNames.size());

        for (int index = 0;
             index < flagNames.size();
             index++) {

            itemFlags.add(
                    parseItemFlag(
                            itemId,
                            propertyRoot
                                    + ".itemFlags["
                                    + index
                                    + "]",
                            flagNames.get(index)
                    )
            );
        }

        return List.copyOf(itemFlags);
    }

    /**
     * ItemFlag名をBukkit ItemFlagへ変換する。
     *
     * @param itemId       アイテム定義ID
     * @param propertyPath ItemFlag要素の完全パス
     * @param flagName     ItemFlag名
     * @return ItemFlag
     */
    private ItemFlag parseItemFlag(
            final String itemId,
            final String propertyPath,
            final String flagName
    ) {
        try {
            return ItemFlag.valueOf(
                    flagName.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidEnumValueException(
                    itemId,
                    propertyPath,
                    flagName,
                    ItemFlag.class
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemDto findById(final String itemId) {
        return getCurrentData().get(itemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ItemDto> findAll() {
        return List.copyOf(
                getCurrentData().values()
        );
    }
}
