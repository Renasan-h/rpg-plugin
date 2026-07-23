package com.example.rpg.event.infrastructure;

import com.example.rpg.event.BusinessEvent;

import java.time.Instant;

/**
 * 外部システムやインフラストラクチャに関する出来事を表すイベント。
 *
 * <p>
 * データベース、Redis、Discord、メールなど、
 * RPGゲームドメイン外の技術的な連携結果を表現する。
 * </p>
 */
public abstract class InfrastructureEvent extends BusinessEvent {

    /**
     * 現在日時を発生日時としてInfrastructureEventを生成する。
     */
    protected InfrastructureEvent() {
        super();
    }

    /**
     * 発生日時を指定してInfrastructureEventを生成する。
     *
     * @param occurredAt イベント発生日時
     */
    protected InfrastructureEvent(final Instant occurredAt) {
        super(occurredAt);
    }
}
