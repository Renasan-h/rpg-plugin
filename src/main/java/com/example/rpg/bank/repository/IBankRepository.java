package com.example.rpg.bank.repository;

import java.util.UUID;

/**
 * プレイヤーの銀行残高を管理するRepositoryです。
 *
 * <p>
 * 銀行残高の取得・更新および永続化処理を抽象化します。
 * 入金や出金などの業務ルールは担当せず、
 * 残高データの保存と取得だけを担当します。
 * </p>
 */
public interface IBankRepository {

    /**
     * プレイヤーの銀行残高を取得します。
     *
     * @param playerId プレイヤーUUID
     * @return 銀行残高、未登録の場合は0
     * @throws NullPointerException playerIdがnullの場合
     */
    int findBalance(UUID playerId);

    /**
     * プレイヤーの銀行残高を設定します。
     *
     * @param playerId プレイヤーUUID
     * @param amount   設定する銀行残高
     * @return 設定後の銀行残高
     * @throws NullPointerException     playerIdがnullの場合
     * @throws IllegalArgumentException amountが負数の場合
     */
    int setBalance(UUID playerId, int amount);
}
