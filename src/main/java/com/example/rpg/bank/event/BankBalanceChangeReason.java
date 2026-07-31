package com.example.rpg.bank.event;

/**
 * 銀行残の変更理由用
 */
public enum BankBalanceChangeReason {

    /**
     * 銀行へ入金
     */
    DEPOSIT,

    /**
     * 銀行から出金
     */
    WITHDRAW,

    /**
     * 他プレイヤーに銀行から振込
     */
    TRANSFER_SENT,

    /**
     * 他プレイヤーから銀行へ振込
     */
    TRANSFER_RECEIVED,

    /**
     * その他
     */
    OTHER
}
