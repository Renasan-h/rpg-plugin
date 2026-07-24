package com.example.rpg.analytics.repository;

import java.util.UUID;

/**
 * プレイヤーの購入統計を管理するRepositoryです。
 *
 * <p>実装方法（メモリ・SQLite・PostgreSQLなど）は
 * Repository実装クラスへ隠蔽します。</p>
 */
public interface PurchaseStatisticsRepository {
    /**
     * 指定した商品の購入回数を1増加します。
     *
     * @param playerId プレイヤーUUID
     * @param itemId   アイテムID
     */
    void incrementPurchaseCount(
            UUID playerId,
            String itemId
    );

    /**
     * 指定した商品の購入回数を取得します。
     *
     * @param playerId プレイヤーUUID
     * @param itemId   アイテムID
     * @return 購入回数
     */
    int getPurchaseCount(
            UUID playerId,
            String itemId
    );

    /**
     * プレイヤーの全購入回数を取得します。
     *
     * @param playerId プレイヤーUUID
     * @return 全購入回数
     */
    int getTotalPurchaseCount(
            UUID playerId
    );

    /**
     * プレイヤーの統計を削除します。
     *
     * @param playerId プレイヤーUUID
     */
    void clearPlayerStatistics(
            UUID playerId
    );

    /**
     * 全プレイヤーの統計を削除します。
     */
    void clearAllStatistics();
}
