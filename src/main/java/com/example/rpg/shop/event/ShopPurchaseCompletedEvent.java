package com.example.rpg.shop.event;

import com.example.rpg.event.domain.DomainEvent;
import com.example.rpg.shop.dto.ShopItemType;
import org.bukkit.event.HandlerList;

import java.util.Objects;
import java.util.UUID;

/**
 * SHOP商品の購入が正常に完了したことを表すDomain Event。
 *
 * <p>
 * 商品の付与および購入履歴の更新が完了した後に発行される。
 * このイベントは購入処理を要求する命令ではなく、
 * 購入が完了したという事実を表す。
 * </p>
 */
public class ShopPurchaseCompletedEvent extends DomainEvent {

    /**
     * Bukkitがイベントリスナーを管理するHandlerList。
     */
    private static final HandlerList HANDLERS =
            new HandlerList();

    /**
     * 購入者のUUID。
     */
    private final UUID playerId;

    /**
     * 購入時点のプレイヤー名
     */
    private final String playerName;

    /**
     * 購入したSHOP商品ID
     */
    private final String shopItemId;

    /**
     * SHOP商品が参照するRPGアイテムID
     *
     * <p>
     * COMMAND商品の場合、設定内容によっては空文字の場合がある
     * </p>
     */
    private final String itemId;

    /**
     * 購入した商品の種別
     */
    private final ShopItemType itemType;

    /**
     * 購入によっては付与された数量
     */
    private final int amount;

    /**
     * 購入時に支払った金額
     */
    private final int paidPrice;

    /**
     * 購入上限
     */
    private final int puchaseLimit;

    /**
     * ShopPurchaseCompletedEventを生成する。
     *
     * @param playerId     購入者UUID
     * @param playerName   購入時点のプレイヤー名
     * @param shopItemId   SHOP商品ID
     * @param itemId       RPGアイテムID
     * @param itemType     商品種別
     * @param amount       付与数量
     * @param paidPrice    支払金額
     * @param puchaseLimit 購入上限
     * @throws NullPointerException     必須引数がnullの場合
     * @throws IllegalArgumentException amountまたはpaidPriceが不正な場合
     */
    public ShopPurchaseCompletedEvent(
            final UUID playerId,
            final String playerName,
            final String shopItemId,
            final String itemId,
            final ShopItemType itemType,
            final int amount,
            final int paidPrice,
            final int puchaseLimit
    ) {
        this.playerId = Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );
        this.playerName = Objects.requireNonNull(
                playerName,
                "playerName must not be null"
        );
        this.shopItemId = Objects.requireNonNull(
                shopItemId,
                "shopItemId must not be null"
        );
        this.itemId = Objects.requireNonNullElse(
                itemId,
                ""
        );
        this.itemType = Objects.requireNonNull(
                itemType,
                "itemType must not be null"
        );

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero"
                            + " / amount="
                            + amount
            );
        }

        if (paidPrice < 0) {
            throw new IllegalArgumentException(
                    "paidPrice must not be negative"
                            + " / paidPrice="
                            + paidPrice
            );
        }

        this.amount = amount;
        this.paidPrice = paidPrice;
        this.puchaseLimit = puchaseLimit;
    }

    /**
     * Bukkitがイベント登録時に使用するHandlerListを取得する。
     *
     * @return HandlerList
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * 購入者UUIDを取得する。
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * 購入時点のプレイヤー名を取得する。
     *
     * @return プレイヤー名
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * 購入したSHOP商品IDを取得する。
     *
     * @return SHOP商品ID
     */
    public String getShopItemId() {
        return shopItemId;
    }

    /**
     * SHOP商品が参照するRPGアイテムIDを取得する。
     *
     * @return RPGアイテムID。未設定の場合は空文字
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * 購入した商品の種別を取得する。
     *
     * @return 商品種別
     */
    public ShopItemType getItemType() {
        return itemType;
    }

    /**
     * 購入によって付与された数量を取得する。
     *
     * @return 付与数量
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 購入時に支払った金額を取得する。
     *
     * @return 支払金額
     */
    public int getPaidPrice() {
        return paidPrice;
    }

    /**
     * 購入上限を取得する。
     *
     * @return 購入上限
     */
    public int getPuchaseLimit() {
        return puchaseLimit;
    }

    /**
     * このイベントのHandlerListを取得する。
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
