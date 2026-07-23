package com.example.rpg.event;

import org.bukkit.event.Event;

import java.time.Instant;
import java.util.Objects;

/**
 * RPGプラグイン内で発生した業務上の事実を表すイベント
 *
 * <p>
 * ゲーム内の状態変化、アプリケーション処理の完了
 * 外部システムとの連携結果などを通知するための基底クラス
 * </p>
 *
 * <p>
 * Bukkitのイベント機構を利用するため、
 * {@link Event} を継承する
 * </p>
 */
public abstract class BusinessEvent extends Event {

    /**
     * イベントが発生した日時
     */
    private final Instant occurredAt;

    /**
     * 同期イベントを生成する。
     *
     * <p>
     * 通常のゲーム処理はMinecraftサーバーのメインスレッド上で
     * 実行されるため、標準では同期イベントとして扱う
     * </p>
     */
    protected BusinessEvent() {
        this(Instant.now());
    }

    /**
     * 発生日時を指定して同期イベントを生成する。
     *
     * <p>
     * 主にテストや、保存済みイベントの復元で使用する。
     * </p>
     *
     * @param occurredAt イベント発生日時
     * @throws NullPointerException occurredAtがnullの場合
     */
    protected BusinessEvent(final Instant occurredAt) {
        // 非同期イベントか？
        super(false);

        this.occurredAt = Objects.requireNonNull(
                occurredAt,
                "occurredAt must not be null"
        );
    }

    /**
     * イベント発生日時を取得する
     *
     * @return イベント発生日時
     */
    public final Instant getOccurredAt() {
        return occurredAt;
    }
}
