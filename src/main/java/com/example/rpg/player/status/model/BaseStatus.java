package com.example.rpg.player.status.model;

/**
 * プレイヤーが素で持つ基礎能力値を表す不変オブジェクト
 *
 * <p>
 * 装備、バフ、デバフ、パッシブスキルなどによる補正は含まない。
 * 基礎能力値の変更が必要な場合は、既存インスタンスを書き換えず、
 * 新しいBaseStatusを生成する。
 * </p>
 */
public final class BaseStatus {

    /**
     * 基礎HP
     */
    private final int hp;

    /**
     * 基礎MP
     */
    private final int mp;

    /**
     * 基礎スタミナ
     */
    private final int stamina;

    /**
     * 基礎物理攻撃力
     */
    private final int attack;

    /**
     * 基礎魔法攻撃力
     */
    private final int magicAttack;

    /**
     * 基礎物理防御力
     */
    private final int defense;

    /**
     * 基礎魔法防御力
     */
    private final int magicDefense;

    /**
     * 筋力
     */
    private final int strength;

    /**
     * 器用さ
     */
    private final int dexterity;

    /**
     * 生命力
     */
    private final int vitality;

    /**
     * 知力
     */
    private final int intelligence;

    /**
     * 運
     */
    private final int luck;

    /**
     * BaseStatusを生成します。
     *
     * @param hp           基礎HP
     * @param mp           基礎MP
     * @param stamina      基礎スタミナ
     * @param attack       基礎物理攻撃力
     * @param magicAttack  基礎魔法攻撃力
     * @param defense      基礎物理防御力
     * @param magicDefense 基礎魔法防御力
     * @param strength     筋力
     * @param dexterity    器用さ
     * @param vitality     生命力
     * @param intelligence 知力
     * @param luck         運
     * @throws IllegalArgumentException いずれかの値が負数の場合
     */
    public BaseStatus(
            final int hp,
            final int mp,
            final int stamina,
            final int attack,
            final int magicAttack,
            final int defense,
            final int magicDefense,
            final int strength,
            final int dexterity,
            final int vitality,
            final int intelligence,
            final int luck
    ) {
        validateNonNegative(
                "hp",
                hp
        );
        validateNonNegative(
                "mp",
                mp
        );
        validateNonNegative(
                "stamina",
                stamina
        );
        validateNonNegative(
                "attack",
                attack
        );
        validateNonNegative(
                "magicAttack",
                magicAttack
        );
        validateNonNegative(
                "defense",
                defense
        );
        validateNonNegative(
                "magicDefense",
                magicDefense
        );
        validateNonNegative(
                "strength",
                strength
        );
        validateNonNegative(
                "dexterity",
                dexterity
        );
        validateNonNegative(
                "vitality",
                vitality
        );
        validateNonNegative(
                "intelligence",
                intelligence
        );
        validateNonNegative(
                "luck",
                luck
        );

        this.hp = hp;
        this.mp = mp;
        this.stamina = stamina;
        this.attack = attack;
        this.magicAttack = magicAttack;
        this.defense = defense;
        this.magicDefense = magicDefense;
        this.strength = strength;
        this.dexterity = dexterity;
        this.vitality = vitality;
        this.intelligence = intelligence;
        this.luck = luck;
    }

    /**
     * 値が0以上であることを検証します。
     *
     * @param propertyName プロパティ名
     * @param value        検証対象値
     * @throws IllegalArgumentException valueが負数の場合
     */
    private static void validateNonNegative(
            final String propertyName,
            final int value
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    propertyName
                            + " must not be negative: "
                            + value
            );
        }
    }

    public int getHp() {
        return hp;
    }

    public int getMp() {
        return mp;
    }

    public int getStamina() {
        return stamina;
    }

    public int getAttack() {
        return attack;
    }

    public int getMagicAttack() {
        return magicAttack;
    }

    public int getDefense() {
        return defense;
    }

    public int getMagicDefense() {
        return magicDefense;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getVitality() {
        return vitality;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getLuck() {
        return luck;
    }
}