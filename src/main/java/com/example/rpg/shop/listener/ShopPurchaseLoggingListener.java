package com.example.rpg.shop.listener;

import com.example.rpg.shop.event.ShopPurchaseCompletedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * SHOP購入完了イベントをサーバーログへ記録するListener。
 *
 * <p>
 * BusinessEvent基盤の動作確認を兼ねた最初のイベント購読処理である。
 * SHOP購入処理そのものには影響を与えない。
 * </p>
 */
public class ShopPurchaseLoggingListener implements Listener {

    /**
     * サーバーログ出力先。
     */
    private final Logger logger;

    /**
     * ShopPurchaseLoggingListenerを生成する。
     *
     * @param logger ログ出力先
     * @throws NullPointerException loggerがnullの場合
     */
    public ShopPurchaseLoggingListener(final Logger logger) {
        this.logger = Objects.requireNonNull(
                logger,
                "logger must not be null"
        );
    }

    /**
     * SHOP購入完了イベントをログへ記録する。
     *
     * @param event SHOP購入完了イベント
     */
    @EventHandler
    public void onShopPurchaseCompleted(
            final ShopPurchaseCompletedEvent event
    ) {
        logger.info(
                "【Shop purchase completed】"
                        + " / playerId="
                        + event.getPlayerId()
                        + " / playerName="
                        + event.getPlayerName()
                        + " / shopItemId="
                        + event.getShopItemId()
                        + " / itemId="
                        + event.getItemId()
                        + " / itemType="
                        + event.getItemType()
                        + " / amount="
                        + event.getAmount()
                        + " / paidPrice="
                        + event.getPaidPrice()
                        + " / puchaseLimit="
                        + event.getPuchaseLimit()
                        + " / occurredAt="
                        + event.getOccurredAt()
        );
    }
}
