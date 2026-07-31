package com.example.rpg.bank.event;

import com.example.rpg.event.BusinessEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * 銀行残高が変更された事を通知するイベント
 */
public class BankBalanceChangedEvent extends BusinessEvent {

    /**
     * Bukkitイベントハンドラー一覧
     */
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * 対象プレイヤーUUID
     */
    private final UUID playerId;

    /**
     * 変更前銀行残高
     */
    private final int beforeBalance;

    /**
     * 変更後銀行残高
     */
    private final int afterBalance;

    /**
     * 銀行残高通知理由
     */
    private final BankBalanceChangeReason reason;


    /**
     * 銀行残高変更イベントを生成します。
     *
     * @param playerId      対象プレイヤーUUID
     * @param beforeBalance 変更前の銀行残高
     * @param afterBalance  変更後の銀行残高
     * @param reason        変更理由
     */
    public BankBalanceChangedEvent(
            final UUID playerId,
            final int beforeBalance,
            final int afterBalance,
            final BankBalanceChangeReason reason
    ) {
        this.playerId = Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        this.reason = Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        this.beforeBalance = beforeBalance;
        this.afterBalance = afterBalance;
    }

    /**
     * Bukkitイベントハンドラー一覧を取得します。
     *
     * @return ハンドラー一覧
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * 対象プレイヤーUUIDを取得します。
     *
     * @return 対象プレイヤーUUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * 変更前の銀行残高を取得します。
     *
     * @return 変更前の銀行残高
     */
    public int getBeforeBalance() {
        return beforeBalance;
    }

    /**
     * 変更後の銀行残高を取得します。
     *
     * @return 変更後の銀行残高
     */
    public int getAfterBalance() {
        return afterBalance;
    }

    /**
     * 銀行残高の変更理由を取得します。
     *
     * @return 変更理由
     */
    public BankBalanceChangeReason getReason() {
        return reason;
    }

    /**
     * Bukkitイベントハンドラー一覧を取得します。
     *
     * @return ハンドラー一覧
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
