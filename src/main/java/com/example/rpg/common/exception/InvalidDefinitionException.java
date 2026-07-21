package com.example.rpg.common.exception;

import java.util.Objects;

/**
 * 設定ファイルから読み込まれた定義の内容が不正な場合に発生する例外。
 *
 * <p>
 * YAMLの型変換には成功したものの、値の範囲や他定義との参照関係など、
 * ドメイン上の制約を満たしていない場合に使用する。
 * </p>
 */
public final class InvalidDefinitionException
        extends IllegalArgumentException {

    /**
     * 不正な定義の種類。
     */
    private final String definitionType;

    /**
     * 不正な定義のID。
     */
    private final String definitionId;

    /**
     * 不正なプロパティ名。
     */
    private final String propertyName;

    /**
     * 不正と判断した理由。
     */
    private final String reason;

    /**
     * 例外を生成する。
     *
     * @param definitionType 定義種別
     * @param definitionId   定義ID
     * @param propertyName   プロパティ名
     * @param reason         不正理由
     */
    public InvalidDefinitionException(
            final String definitionType,
            final String definitionId,
            final String propertyName,
            final String reason
    ) {
        super(createMessage(
                definitionType,
                definitionId,
                propertyName,
                reason
        ));

        this.definitionType = Objects.requireNonNull(
                definitionType,
                "definitionType must not be null"
        );
        this.definitionId = Objects.requireNonNull(
                definitionId,
                "definitionId must not be null"
        );
        this.propertyName = Objects.requireNonNull(
                propertyName,
                "propertyName must not be null"
        );
        this.reason = Objects.requireNonNull(
                reason,
                "reason must not be null"
        );
    }

    /**
     * 例外メッセージを生成する。
     *
     * @param definitionType 定義種別
     * @param definitionId   定義ID
     * @param propertyName   プロパティ名
     * @param reason         不正理由
     * @return 例外メッセージ
     */
    private static String createMessage(
            final String definitionType,
            final String definitionId,
            final String propertyName,
            final String reason
    ) {
        return "Invalid definition"
                + " / type="
                + definitionType
                + " / id="
                + definitionId
                + " / property="
                + propertyName
                + " / reason="
                + reason;
    }

    /**
     * 定義種別を取得する。
     *
     * @return 定義種別
     */
    public String getDefinitionType() {
        return definitionType;
    }

    /**
     * 定義IDを取得する。
     *
     * @return 定義ID
     */
    public String getDefinitionId() {
        return definitionId;
    }

    /**
     * プロパティ名を取得する。
     *
     * @return プロパティ名
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 不正理由を取得する。
     *
     * @return 不正理由
     */
    public String getReason() {
        return reason;
    }
}