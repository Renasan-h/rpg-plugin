package com.example.rpg.money.exception;

import java.util.UUID;

/**
 * プレイヤーの所持金が必要額を下回っている場合に発生する例外です。
 */
public final class InsufficientMoneyException
        extends RuntimeException {

    /**
     * 所持金不足例外を生成します。
     *
     * @param playerId       プレイヤーUUID
     * @param currentAmount  現在の所持金
     * @param requiredAmount 必要な金額
     */
    public InsufficientMoneyException(
            final UUID playerId,
            final int currentAmount,
            final int requiredAmount
    ) {
        super(
                "Insufficient money"
                        + " / playerId=" + playerId
                        + " / currentAmount=" + currentAmount
                        + " / requiredAmount=" + requiredAmount
        );
    }
}