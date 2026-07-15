package com.example.rpg.item.dto;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Objects;

/**
 * RPGアイテムの定義情報を保持するDTO。
 *
 * <p>
 * アイテム自体のMaterial・表示名・Loreのみを保持し、
 * SHOP価格・購入上限・販売権限などの販売情報は保持しない。
 * </p>
 */
public class ItemDto {
    /**
     * アイテムを一意に識別するID。
     */
    private final String id;

    /**
     * Minecraft上で使用するMaterial。
     */
    private final Material material;

    /**
     * MiniMessage形式の表示名。
     */
    private final String displayName;

    /**
     * MiniMessage形式のLore。
     */
    private final List<String> lore;

    /**
     * ItemFlags。
     */
    private final List<ItemFlag> itemFlags;

    /**
     * 耐久値を減少させないか。
     */
    private final boolean unbreakable;

    /**
     * リソースパックのモデル選択に使用するCustomModelData。
     *
     * <p>
     * 未指定の場合はnull。
     * </p>
     */
    private final Integer customModelData;

    /**
     * アイテムへ設定するエンチャント一覧。
     */
    private final List<ItemEnchantDto> enchantments;

    /**
     * ItemDtoを生成する。
     *
     * @param id              ItemId
     * @param material        Minecraft Material
     * @param displayName     MiniMessage形式の表示名
     * @param lore            MiniMessage形式のLore
     * @param itemFlags       ItemFlag一覧
     * @param unbreakable     耐久値を減少させない場合true
     * @param customModelData CustomModelData。未指定の場合はnull
     * @param enchantments    エンチャント一覧
     * @throws NullPointerException 引数がnullの場合
     */
    public ItemDto(
            final String id,
            final Material material,
            final String displayName,
            final List<String> lore,
            final List<ItemFlag> itemFlags,
            final boolean unbreakable,
            final Integer customModelData,
            final List<ItemEnchantDto> enchantments
    ) {
        this.id = Objects.requireNonNull(
                id,
                "id must not be null"
        );
        this.material = Objects.requireNonNull(
                material,
                "material must not be null"
        );
        this.displayName = Objects.requireNonNull(
                displayName,
                "displayName must not be null"
        );
        this.lore = List.copyOf(
                Objects.requireNonNull(
                        lore,
                        "lore must not be null"
                )
        );
        this.itemFlags = List.copyOf(
                Objects.requireNonNull(
                        itemFlags,
                        "itemFlags must not be null"
                )
        );
        this.unbreakable = unbreakable;
        this.customModelData = customModelData;
        this.enchantments = List.copyOf(
                Objects.requireNonNull(
                        enchantments,
                        "enchantments must not be null"
                )
        );
    }

    /**
     * アイテムIDを取得する。
     *
     * @return アイテムID
     */
    public String getId() {
        return id;
    }

    /**
     * Minecraft Materialを取得する。
     *
     * @return Material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * MiniMessage形式の表示名を取得する。
     *
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * MiniMessage形式のLoreを取得する。
     *
     * @return 変更不可能なLore一覧
     */
    public List<String> getLore() {
        return lore;
    }

    /**
     * ItemFlag一覧を取得する。
     *
     * @return 変更不可能なItemFlag一覧
     */
    public List<ItemFlag> getItemFlags() {
        return itemFlags;
    }

    /**
     * 耐久値を減少させないか。
     *
     * @return unbreakable設定
     */
    public boolean isUnbreakable() {
        return unbreakable;
    }

    /**
     * CustomModelDataを取得する。
     *
     * @return CustomModelData。未指定の場合はnull
     */
    public Integer getCustomModelData() {
        return customModelData;
    }

    /**
     * エンチャント一覧を取得する。
     *
     * @return 変更不可能なエンチャント一覧
     */
    public List<ItemEnchantDto> getEnchantments() {
        return enchantments;
    }

}
