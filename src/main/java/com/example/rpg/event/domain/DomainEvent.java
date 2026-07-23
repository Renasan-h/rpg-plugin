package com.example.rpg.event.domain;

import com.example.rpg.event.BusinessEvent;

import java.time.Instant;

/**
 * ゲーム世界で発生した事実を表すイベント。
 *
 * <p>
 * プレイヤーによる購入、売却、レベルアップ、
 * クエスト完了などのドメイン上の出来事を表現する。
 * </p>
 *
 * <p>
 * DomainEventは「何かを実行させる命令」ではなく、
 * 「すでに発生した事実」を表す。
 * </p>
 */
public abstract class DomainEvent extends BusinessEvent {

    /**
     * 現在日時を発生日時としてDomainEventを生成する。
     */
    protected DomainEvent() {
        super();
    }

    /**
     * 発生日時を指定してDomainEventを生成する。
     *
     * @param occurredAt イベント発生日時
     */
    protected DomainEvent(final Instant occurredAt) {
        super(occurredAt);
    }
}
