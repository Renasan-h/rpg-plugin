package com.example.rpg.item.dto;

import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * アイテムへ設定するPotionEffect情報を保持するDTO。
 */
public class ItemEffectDto {

    /**
     * PotionEffectの種類。
     */
    private final PotionEffectType effectType;

    /**
     * 効果時間。単位はtick。
     */
    private final int duration;

    /**
     * 効果レベル。0がレベル1。
     */
    private final int amplifier;

    /**
     * Ambient表示を使用するか。
     */
    private final boolean ambient;

    /**
     * パーティクルを表示するか。
     */
    private final boolean particles;

    /**
     * 効果アイコンを表示するか。
     */
    private final boolean icon;

    /**
     * ItemEffectDtoを生成する。
     *
     * @param effectType PotionEffectType
     * @param duration   効果時間
     * @param amplifier  効果レベル
     * @param ambient    Ambient表示
     * @param particles  パーティクル表示
     * @param icon       アイコン表示
     */
    public ItemEffectDto(
            final PotionEffectType effectType,
            final int duration,
            final int amplifier,
            final boolean ambient,
            final boolean particles,
            final boolean icon
    ) {
        this.effectType = Objects.requireNonNull(
                effectType,
                "effectType must not be null"
        );
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isParticles() {
        return particles;
    }

    public boolean isIcon() {
        return icon;
    }
}