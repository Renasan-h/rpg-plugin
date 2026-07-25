package com.example.rpg.money.event;

import com.example.rpg.event.domain.DomainEvent;
import org.bukkit.event.HandlerList;

import java.util.Objects;
import java.util.UUID;

/**
 * プレイヤーの所持金が変更されたことを通知するドメインイベント
 *
 * <p>
 * このイベントは所持金の変更処理そのものはを行わない。
 * 所持金の変更が正常に完了した後、その結果を外部へ通知するために使用する。
 * </p>
 */
public final class MoneyChangedEvent extends DomainEvent {

    /**
     * Bukkitがイベントハンドラーを管理するための一覧
     */
    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * 所持金が変更されたプレイヤーのUUID
     */
    private final UUID playerId;

    /**
     * 変更前の所持金
     */
    private final int beforeMoney;

    /**
     * 変更後の所持金
     */
    private final int afterMoney;

    /**
     * 所持金の変更額
     *
     * <p>増加の場合は正数、減少の場合は負数</p>
     */
    private final int changeMoney;

    /**
     * 変更された理由
     */
    private final MoneyChangeReason reason;

    /**
     * 所持金変更イベントを生成する
     *
     * @param playerId    プレイヤーID
     * @param beforeMoney 変更前の所持金
     * @param afterMoney  変更後の所持金
     * @param reason      変更理由
     * @throws NullPointerException     playerIdまたはreasonがnullの場合
     * @throws IllegalArgumentException 所持金が負数の場合
     */
    public MoneyChangedEvent(
            final UUID playerId,
            final int beforeMoney,
            final int afterMoney,
            final MoneyChangeReason reason
    ) {
        if (beforeMoney < 0) {
            throw new IllegalArgumentException(
                    "beforeAmount must not be negative: "
                            + beforeMoney
            );
        }

        if (afterMoney < 0) {
            throw new IllegalArgumentException(
                    "afterAmount must not be negative: "
                            + afterMoney
            );
        }

        this.playerId = Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );
        this.beforeMoney = beforeMoney;
        this.afterMoney = afterMoney;
        this.changeMoney = afterMoney - beforeMoney;
        this.reason = Objects.requireNonNull(
                reason,
                "reason must not be null"
        );
    }

    /**
     * Bukkitがイベント登録時に使用するハンドラー一覧を取得します。
     *
     * @return ハンドラー一覧
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * 所持金が変更されたプレイヤーのUUIDを取得します。
     *
     * @return プレイヤーUUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * 変更前の所持金を取得します。
     *
     * @return 変更前の所持金
     */
    public int getBeforeMoney() {
        return beforeMoney;
    }

    /**
     * 変更後の所持金を取得します。
     *
     * @return 変更後の所持金
     */
    public int getAfterMoney() {
        return afterMoney;
    }

    /**
     * 所持金の増減値を取得します。
     *
     * <p>増加の場合は正数、減少の場合は負数です。</p>
     *
     * @return 所持金の増減値
     */
    public int getChangeMoney() {
        return changeMoney;
    }

    /**
     * 所持金が変更された理由を取得します。
     *
     * @return 所持金変更理由
     */
    public MoneyChangeReason getReason() {
        return reason;
    }

    /**
     * このイベントに登録されたハンドラー一覧を取得します。
     *
     * @return ハンドラー一覧
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
