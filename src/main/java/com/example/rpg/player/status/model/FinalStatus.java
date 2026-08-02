package com.example.rpg.player.status.model;

/**
 * 装備、バフ、デバフ、パッシブなどの補正を反映した
 * 最終ステータスを表す不変オブジェクト
 *
 * <p>
 * 本クラスはStatusCalculatorによる計算結果として生成されます。
 * 永続化対象ではなく、必要なタイミングで再計算します。
 * </p>
 *
 * <p>
 * クリティカル率、クリティカルダメージ、命中率、回避率、
 * 攻撃速度、移動速度は、10,000を100.00%とする整数で保持します。
 * </p>
 */
public final class FinalStatus {

    /**
     * 割合値における100.00%を表す基準値
     */
    public static final int RATE_SCALE = 10_000;

    /**
     * 補正後HP
     */
    private final int hp;

    /**
     * 補正後MP
     */
    private final int mp;

    /**
     * 補正後スタミナ
     */
    private final int stamina;

    /**
     * 補正後物理攻撃力
     */
    private final int attack;

    /**
     * 補正後魔法攻撃力
     */
    private final int magicAttack;

    /**
     * 補正後物理防御力
     */
    private final int defense;

    /**
     * 補正後魔法防御力
     */
    private final int magicDefense;

    /**
     * 補正後筋力
     */
    private final int strength;

    /**
     * 補正後器用さ
     */
    private final int dexterity;

    /**
     * 補正後生命力
     */
    private final int vitality;

    /**
     * 補正後知力
     */
    private final int intelligence;

    /**
     * 補正後運
     */
    private final int luck;

    /**
     * クリティカル率
     *
     * <p>10,000を100.00%として扱います。</p>
     */
    private final int criticalRate;

    /**
     * クリティカル発生時のダメージ倍率
     *
     * <p>
     * 10,000を100.00%として扱います。
     * 例えば15,000は通常ダメージの150.00%です。
     * </p>
     */
    private final int criticalDamage;

    /**
     * 命中率
     *
     * <p>10,000を100.00%として扱います。</p>
     */
    private final int accuracy;

    /**
     * 回避率
     *
     * <p>10,000を100.00%として扱います。</p>
     */
    private final int evasion;

    /**
     * 攻撃速度倍率
     *
     * <p>10,000を100.00%として扱います。</p>
     */
    private final int attackSpeed;

    /**
     * 移動速度倍率
     *
     * <p>10,000を100.00%として扱います。</p>
     */
    private final int moveSpeed;

    /**
     * FinalStatusを生成します。
     *
     * @param hp             補正後HP
     * @param mp             補正後MP
     * @param stamina        補正後スタミナ
     * @param attack         補正後物理攻撃力
     * @param magicAttack    補正後魔法攻撃力
     * @param defense        補正後物理防御力
     * @param magicDefense   補正後魔法防御力
     * @param strength       補正後筋力
     * @param dexterity      補正後器用さ
     * @param vitality       補正後生命力
     * @param intelligence   補正後知力
     * @param luck           補正後運
     * @param criticalRate   クリティカル率
     * @param criticalDamage クリティカルダメージ倍率
     * @param accuracy       命中率
     * @param evasion        回避率
     * @param attackSpeed    攻撃速度倍率
     * @param moveSpeed      移動速度倍率
     * @throws IllegalArgumentException いずれかの値が負数の場合
     */
    public FinalStatus(
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
            final int luck,
            final int criticalRate,
            final int criticalDamage,
            final int accuracy,
            final int evasion,
            final int attackSpeed,
            final int moveSpeed
    ) {
        validateNonNegative("hp", hp);
        validateNonNegative("mp", mp);
        validateNonNegative("stamina", stamina);
        validateNonNegative("attack", attack);
        validateNonNegative("magicAttack", magicAttack);
        validateNonNegative("defense", defense);
        validateNonNegative("magicDefense", magicDefense);
        validateNonNegative("strength", strength);
        validateNonNegative("dexterity", dexterity);
        validateNonNegative("vitality", vitality);
        validateNonNegative("intelligence", intelligence);
        validateNonNegative("luck", luck);
        validateNonNegative("criticalRate", criticalRate);
        validateNonNegative("criticalDamage", criticalDamage);
        validateNonNegative("accuracy", accuracy);
        validateNonNegative("evasion", evasion);
        validateNonNegative("attackSpeed", attackSpeed);
        validateNonNegative("moveSpeed", moveSpeed);

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
        this.criticalRate = criticalRate;
        this.criticalDamage = criticalDamage;
        this.accuracy = accuracy;
        this.evasion = evasion;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
    }

    /**
     * 指定された値が0以上であることを検証します。
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

    /**
     * 補正後HPを取得します。
     *
     * @return 補正後HP
     */
    public int getHp() {
        return hp;
    }

    /**
     * 補正後MPを取得します。
     *
     * @return 補正後MP
     */
    public int getMp() {
        return mp;
    }

    /**
     * 補正後スタミナを取得します。
     *
     * @return 補正後スタミナ
     */
    public int getStamina() {
        return stamina;
    }

    /**
     * 補正後物理攻撃力を取得します。
     *
     * @return 補正後物理攻撃力
     */
    public int getAttack() {
        return attack;
    }

    /**
     * 補正後魔法攻撃力を取得します。
     *
     * @return 補正後魔法攻撃力
     */
    public int getMagicAttack() {
        return magicAttack;
    }

    /**
     * 補正後物理防御力を取得します。
     *
     * @return 補正後物理防御力
     */
    public int getDefense() {
        return defense;
    }

    /**
     * 補正後魔法防御力を取得します。
     *
     * @return 補正後魔法防御力
     */
    public int getMagicDefense() {
        return magicDefense;
    }

    /**
     * 補正後筋力を取得します。
     *
     * @return 補正後筋力
     */
    public int getStrength() {
        return strength;
    }

    /**
     * 補正後器用さを取得します。
     *
     * @return 補正後器用さ
     */
    public int getDexterity() {
        return dexterity;
    }

    /**
     * 補正後生命力を取得します。
     *
     * @return 補正後生命力
     */
    public int getVitality() {
        return vitality;
    }

    /**
     * 補正後知力を取得します。
     *
     * @return 補正後知力
     */
    public int getIntelligence() {
        return intelligence;
    }

    /**
     * 補正後運を取得します。
     *
     * @return 補正後運
     */
    public int getLuck() {
        return luck;
    }

    /**
     * クリティカル率を取得します。
     *
     * @return クリティカル率
     */
    public int getCriticalRate() {
        return criticalRate;
    }

    /**
     * クリティカルダメージ倍率を取得します。
     *
     * @return クリティカルダメージ倍率
     */
    public int getCriticalDamage() {
        return criticalDamage;
    }

    /**
     * 命中率を取得します。
     *
     * @return 命中率
     */
    public int getAccuracy() {
        return accuracy;
    }

    /**
     * 回避率を取得します。
     *
     * @return 回避率
     */
    public int getEvasion() {
        return evasion;
    }

    /**
     * 攻撃速度倍率を取得します。
     *
     * @return 攻撃速度倍率
     */
    public int getAttackSpeed() {
        return attackSpeed;
    }

    /**
     * 移動速度倍率を取得します。
     *
     * @return 移動速度倍率
     */
    public int getMoveSpeed() {
        return moveSpeed;
    }
}