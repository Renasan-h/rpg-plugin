package com.example.rpg.player.status.modifier;

/**
 * 補正対象となるステータスの種類を表します。
 */
public enum StatusType {

    /**
     * HP
     */
    HP,

    /**
     * MP
     */
    MP,

    /**
     * スタミナ
     */
    STAMINA,

    /**
     * 物理攻撃力
     */
    ATTACK,

    /**
     * 魔法攻撃力
     */
    MAGIC_ATTACK,

    /**
     * 物理防御力
     */
    DEFENSE,

    /**
     * 魔法防御力
     */
    MAGIC_DEFENSE,

    /**
     * 筋力
     */
    STRENGTH,

    /**
     * 器用さ
     */
    DEXTERITY,

    /**
     * 生命力
     */
    VITALITY,

    /**
     * 知力
     */
    INTELLIGENCE,

    /**
     * 運
     */
    LUCK,

    /**
     * クリティカル率
     */
    CRITICAL_RATE,

    /**
     * クリティカルダメージ倍率
     */
    CRITICAL_DAMAGE,

    /**
     * 命中率
     */
    ACCURACY,

    /**
     * 回避率
     */
    EVASION,

    /**
     * 攻撃速度倍率
     */
    ATTACK_SPEED,

    /**
     * 移動速度倍率
     */
    MOVE_SPEED
}