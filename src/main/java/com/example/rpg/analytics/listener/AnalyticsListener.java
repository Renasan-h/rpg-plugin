package com.example.rpg.analytics.listener;

import com.example.rpg.analytics.repository.PurchaseStatisticsRepository;
import com.example.rpg.shop.event.ShopPurchaseCompletedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.logging.Logger;

public class AnalyticsListener implements Listener {

    /**
     * 購入統計Repository
     */
    private final PurchaseStatisticsRepository statisticsRepository;

    /**
     * ロガー
     */
    private final Logger logger;

    /**
     * コンストラクタ
     *
     * @param statisticsRepository 購入統計Repository
     * @param logger               ロガー
     * @throws NullPointerException 引数がnullの場合
     */
    public AnalyticsListener(
            final PurchaseStatisticsRepository statisticsRepository,
            final Logger logger
    ) {
        this.statisticsRepository = Objects.requireNonNull(statisticsRepository);
        this.logger = Objects.requireNonNull(logger);
    }

    /**
     * ショップ購入イベントを受信します。
     *
     * @param event 購入イベント
     */
    @EventHandler
    public void onShopPurchaseCompleted(
            final ShopPurchaseCompletedEvent event
    ) {
        statisticsRepository.incrementPurchaseCount(
                event.getPlayerId(),
                event.getItemId()
        );

        final int purchaseCount = statisticsRepository.getPurchaseCount(
                event.getPlayerId(),
                event.getItemId()
        );

        logger.info(() ->
                "[Analytics] player="
                        + event.getPlayerName()
                        + ", item="
                        + event.getItemId()
                        + ", purchaseCount="
                        + purchaseCount
        );
    }
}
