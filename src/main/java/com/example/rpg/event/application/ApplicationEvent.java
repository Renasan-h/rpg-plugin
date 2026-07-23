package com.example.rpg.event.application;

import com.example.rpg.event.BusinessEvent;

import java.time.Instant;

/**
 * アプリケーション処理上の出来事を表すイベント。
 *
 * <p>
 * 設定の再読み込み、プレイヤーデータのロード、
 * プラグイン初期化など、特定のゲームドメインに属さない
 * アプリケーション上の処理結果を表現する。
 * </p>
 */
public abstract class ApplicationEvent extends BusinessEvent {

    /**
     * 現在日時を発生日時としてApplicationEventを生成する。
     */
    protected ApplicationEvent() {
        super();
    }

    /**
     * 発生日時を指定してApplicationEventを生成する。
     *
     * @param occurredAt イベント発生日時
     */
    protected ApplicationEvent(final Instant occurredAt) {
        super(occurredAt);
    }
}
