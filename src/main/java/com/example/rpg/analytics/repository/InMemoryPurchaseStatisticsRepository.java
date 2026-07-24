package com.example.rpg.analytics.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 購入統計をメモリ上で管理するRepository実装です。
 *
 * <p>サーバー停止時にデータは破棄されます。
 * 将来的にはSQLiteやPostgreSQL実装へ差し替えることを想定しています。</p>
 */
public class InMemoryPurchaseStatisticsRepository implements PurchaseStatisticsRepository {

    /**
     * プレイヤー毎の商品購入回数
     */
    private final Map<UUID, Map<String, Integer>> purchaseStatistics =
            new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void incrementPurchaseCount(
            final UUID playerId,
            final String itemId
    ) {
        purchaseStatistics.computeIfAbsent(
                        playerId,
                        ignored -> new ConcurrentHashMap<>()
                )
                .merge(
                        itemId,
                        1,
                        Integer::sum
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPurchaseCount(
            final UUID playerId,
            final String itemid
    ) {
        return purchaseStatistics.getOrDefault(
                playerId,
                Map.of()
        ).getOrDefault(
                itemid,
                0
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalPurchaseCount(
            final UUID playerId
    ) {
        return purchaseStatistics.getOrDefault(
                        playerId,
                        Map.of()
                ).values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearPlayerStatistics(
            final UUID playerId
    ) {
        purchaseStatistics.remove(playerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAllStatistics() {
        purchaseStatistics.clear();
    }
}
