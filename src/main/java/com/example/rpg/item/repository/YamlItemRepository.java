package com.example.rpg.item.repository;


import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

/**
 * items.ymlからRPGアイテム定義を読み込むRepository。
 *
 * <p>
 * YAMLの構造解析とDTOへの変換を担当する。
 * 読み込んだアイテムはRepository内部のMapへ保持し、
 * 呼び出し側へMapを公開しない。
 * </p>
 */
public class YamlItemRepository implements IItemRepository {
    /**
     * アイテム定義のルートセクション名。
     */
    private static final String ITEMS_SECTION_PATH = "items";

    /**
     * items.ymlの読み込み結果。
     */
    private final YamlConfiguration config;

    /**
     * 読み込み済みアイテム定義。
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
            throw new IllegalArgumentException(
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
     */
    private ItemDto loadItem(
            final String itemId,
            final ConfigurationSection section
    ) {
        final String materialName = section.getString("material", "STONE");

        final Material material = Material.matchMaterial(materialName);

        if (material == null) {
            throw new IllegalArgumentException(
                    "不正なMaterialです: "
                            + materialName
                            + " / itemId="
                            + itemId
            );
        }

        final String displayName = section.getString("displayName", "<white>" + itemId + "</white>");

        final List<String> lore = section.getStringList("lore");

        final List<ItemFlag> itemFlags = loadItemFlags(
                itemId,
                section
        );

        return new ItemDto(
                itemId,
                material,
                displayName,
                lore,
                itemFlags
        );
    }

    /**
     * YAMLからItemFlag一覧を読み込む。
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
     * @throws IllegalArgumentException 未対応のItemFlagが指定された場合
     */
    private ItemFlag parseItemFlag(
            final String itemId,
            final String flagName
    ) {
        try {
            return ItemFlag.valueOf(flagName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown ItemFlag: "
                            + flagName
                            + " / itemId="
                            + itemId,
                    ex
            );
        }
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
