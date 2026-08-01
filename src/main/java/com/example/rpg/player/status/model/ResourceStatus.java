package com.example.rpg.player.status.model;

/**
 * プレイヤーが現在保持しているリソース量を表す不変オブジェクトです。
 *
 * <p>
 * HP、MP、スタミナの現在値のみを保持します。
 * 基礎値や装備・バフによる補正後の上限値は保持しません。
 * </p>
 */
public final class ResourceStatus {

    /**
     * 現在HP
     */
    private final int hp;

    /**
     * 現在MP
     */
    private final int mp;

    /**
     * 現在スタミナ
     */
    private final int stamina;

    /**
     * ResourceStatusを生成します。
     *
     * @param hp      現在HP
     * @param mp      現在MP
     * @param stamina 現在スタミナ
     * @throws IllegalArgumentException いずれかの値が負数の場合
     */
    public ResourceStatus(
            final int hp,
            final int mp,
            final int stamina
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

        this.hp = hp;
        this.mp = mp;
        this.stamina = stamina;
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

    /**
     * 現在HPを取得します。
     *
     * @return 現在HP
     */
    public int getHp() {
        return hp;
    }

    /**
     * 現在MPを取得します。
     *
     * @return 現在MP
     */
    public int getMp() {
        return mp;
    }

    /**
     * 現在スタミナを取得します。
     *
     * @return 現在スタミナ
     */
    public int getStamina() {
        return stamina;
    }

    /**
     * HPを指定値へ変更した新しいResourceStatusを生成します。
     *
     * @param newHp 新しい現在HP
     * @return HPだけを変更した新しいResourceStatus
     */
    public ResourceStatus withHp(final int newHp) {
        return new ResourceStatus(
                newHp,
                mp,
                stamina
        );
    }

    /**
     * MPを指定値へ変更した新しいResourceStatusを生成します。
     *
     * @param newMp 新しい現在MP
     * @return MPだけを変更した新しいResourceStatus
     */
    public ResourceStatus withMp(final int newMp) {
        return new ResourceStatus(
                hp,
                newMp,
                stamina
        );
    }

    /**
     * スタミナを指定値へ変更した新しいResourceStatusを生成します。
     *
     * @param newStamina 新しい現在スタミナ
     * @return スタミナだけを変更した新しいResourceStatus
     */
    public ResourceStatus withStamina(
            final int newStamina
    ) {
        return new ResourceStatus(
                hp,
                mp,
                newStamina
        );
    }
}