package com.example.rpg.item.dto;

import org.bukkit.potion.PotionEffectType;

import java.util.Objects;

/**
 * アイテムへ設定するPotionEffect情報を保持するDTO。
 */
public class ItemEffectDto {

    /**
     * PotionEffectID
     */
    private final String effectId;
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
     * @param effectId   効果ID
     * @param effectType PotionEffectType
     * @param duration   効果時間
     * @param amplifier  効果レベル
     * @param ambient    Ambient表示
     * @param particles  パーティクル表示
     * @param icon       アイコン表示
     */
    public ItemEffectDto(
            final String effectId,
            final PotionEffectType effectType,
            final int duration,
            final int amplifier,
            final boolean ambient,
            final boolean particles,
            final boolean icon
    ) {
        this.effectId = Objects.requireNonNull(
                effectId,
                "effectId must not be null"
        );
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

    /**
     * 効果IDを取得する。
     *
     * @return 効果ID
     */
    public String getEffectId() {
        return effectId;
    }

    /**
     * PotionEffectTypeを取得する。
     *
     * @return PotionEffectType
     */
    public PotionEffectType getEffectType() {
        return effectType;
    }

    /**
     * 効果時間を取得する。
     *
     * @return 効果時間
     */
    public int getDuration() {
        return duration;
    }

    /**
     * 効果レベルを取得する。
     *
     * @return 効果レベル
     */
    public int getAmplifier() {
        return amplifier;
    }

    /**
     * Ambient表示を取得する。
     *
     * @return Ambient表示
     */
    public boolean isAmbient() {
        return ambient;
    }

    /**
     * パーティクル表示を取得する。
     *
     * @return パーティクル表示
     */
    public boolean isParticles() {
        return particles;
    }

    /**
     * 効果アイコン表示有無を取得する。
     *
     * @return 効果アイコンの表示有無
     */
    public boolean isIcon() {
        return icon;
    }
}