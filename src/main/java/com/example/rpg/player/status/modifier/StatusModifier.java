package com.example.rpg.player.status.modifier;

import java.util.Objects;

/**
 * 1つのステータスに対する補正内容を表す不変オブジェクトです。
 *
 * <p>
 * 1インスタンスは、1種類のステータスに対する
 * 1つの演算だけを表します。
 * </p>
 *
 * <p>
 * 装備、バフ、デバフ、パッシブスキルなど、
 * 補正元の種類には依存しません。
 * </p>
 */
public final class StatusModifier {

    /**
     * 補正対象ステータス
     */
    private final StatusType statusType;

    /**
     * 補正演算
     */
    private final ModifierOperation operation;

    /**
     * 補正値
     *
     * <p>
     * ADDの場合は固定値として扱います。
     * MULTIPLYの場合は10,000を100.00%とする割合値として扱います。
     * </p>
     */
    private final int value;

    /**
     * StatusModifierを生成します。
     *
     * @param statusType 補正対象ステータス
     * @param operation  補正演算
     * @param value      補正値
     * @throws NullPointerException statusTypeまたはoperationがnullの場合
     */
    public StatusModifier(
            final StatusType statusType,
            final ModifierOperation operation,
            final int value
    ) {
        this.statusType = Objects.requireNonNull(
                statusType,
                "statusType must not be null"
        );

        this.operation = Objects.requireNonNull(
                operation,
                "operation must not be null"
        );

        this.value = value;
    }

    /**
     * 補正対象ステータスを取得します。
     *
     * @return 補正対象ステータス
     */
    public StatusType getStatusType() {
        return statusType;
    }

    /**
     * 補正演算を取得します。
     *
     * @return 補正演算
     */
    public ModifierOperation getOperation() {
        return operation;
    }

    /**
     * 補正値を取得します。
     *
     * @return 補正値
     */
    public int getValue() {
        return value;
    }
}