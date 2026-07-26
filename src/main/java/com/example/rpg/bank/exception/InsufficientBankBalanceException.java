package com.example.rpg.bank.exception;

import java.util.UUID;

/**
 * 銀行残高が不足している場合に送出される例外です。
 */
public final class InsufficientBankBalanceException
        extends RuntimeException {

    /**
     * 銀行残高不足例外を生成します。
     *
     * @param playerId       対象プレイヤーUUID
     * @param currentBalance 現在の銀行残高
     * @param requiredAmount 必要な金額
     */
    public InsufficientBankBalanceException(
            final UUID playerId,
            final int currentBalance,
            final int requiredAmount
    ) {
        super(
                "Insufficient bank balance."
                        + " playerId="
                        + playerId
                        + ", currentBalance="
                        + currentBalance
                        + ", requiredAmount="
                        + requiredAmount
        );
    }
}