package com.example.rpg.item.dto;

import org.bukkit.enchantments.Enchantment;

import java.util.Objects;

/**
 * RPGアイテムへ設定するエンチャント情報を保持するDTO
 */
public class ItemEnchantmentDto {

    /**
     * 設定するエンチャント
     */
    private final Enchantment enchantment;

    /**
     * エンチャントレベル
     */
    private final int level;

    /**
     * Minecraft標準のレベル制限を無視するか
     */
    private final boolean ignoreLevelRestriction;

    /**
     * ItemEnchantDtoを生成する。
     *
     * @param enchantment            エンチャント
     * @param level                  エンチャントレベル
     * @param ignoreLevelRestriction レベル制限を無視する場合true
     */
    public ItemEnchantmentDto(
            final Enchantment enchantment,
            final int level,
            final boolean ignoreLevelRestriction
    ) {
        this.enchantment = Objects.requireNonNull(
                enchantment,
                "enchantment must not be null"
        );
        this.level = level;
        this.ignoreLevelRestriction = ignoreLevelRestriction;
    }

    /**
     * エンチャントを取得する。
     *
     * @return エンチャント
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * エンチャントレベルを取得する。
     *
     * @return エンチャントレベル
     */
    public int getLevel() {
        return level;
    }

    /**
     * レベル制限を無視するか取得する。
     *
     * @return レベル制限を無視する場合true
     */
    public boolean isIgnoreLevelRestriction() {
        return ignoreLevelRestriction;
    }
}
