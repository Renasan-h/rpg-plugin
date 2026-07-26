package com.example.rpg.money.event;

/**
 * 所持金が変更された理由を表します。
 *
 * <p>所持金の変更理由を文字列ではなく列挙型で管理することで、
 * タイプミスや表記揺れを防止します。</p>
 */
public enum MoneyChangeReason {

    /**
     * モンスター討伐報酬による増加
     */
    MONSTER_REWARD,

    /**
     * リスポーンによる減少
     */
    RESPAWN_PENALTY,

    /**
     * ショップ購入による減少
     */
    SHOP_PURCHASE,

    /**
     * ショップ売却による増加
     */
    SHOP_SELL,

    /**
     * プレイヤー間送金に送金
     */
    PLAYER_PAYMENT_SENT,

    /**
     * プレイヤー間送金による受取
     */
    PLAYER_PAYMENT_RECEIVED,

    /**
     * 銀行への入金による手持ち所持金の減少
     */
    BANK_DEPOSIT,

    /**
     * 銀行からの出金による手持ち所持金の増加
     */
    BANK_WITHDRAW,
    
    /**
     * 管理者による加算
     */
    ADMIN_ADD,

    /**
     * 管理者による減算
     */
    ADMIN_REMOVE,

    /**
     * 管理者による金額の直接設定
     */
    ADMIN_SET,

    /**
     * その他の理由
     */
    OTHER
}
