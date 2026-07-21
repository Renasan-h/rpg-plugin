package com.example.rpg.item.exception;

import java.util.Objects;

/**
 * RPGアイテム定義の設定内容が不正な場合に発生する例外。
 *
 * <p>
 * 設定値そのものは読み込めるものの、複数の設定を組み合わせた結果、
 * Bukkitのアイテムとして成立しない場合に使用する。
 * </p>
 *
 * <p>
 * 使用例:
 * </p>
 *
 * <ul>
 *     <li>PotionMetaを持たないMaterialにPotionEffectが設定されている</li>
 *     <li>アイテムとして使用できないMaterialが設定されている</li>
 *     <li>Materialと属性設定の組み合わせが不正である</li>
 * </ul>
 */
public final class InvalidItemDefinitionException
        extends IllegalArgumentException {

    /**
     * 不正な定義を持つアイテムID。
     */
    private final String itemId;

    /**
     * 不正と判断されたプロパティ名。
     */
    private final String propertyName;

    /**
     * 不正と判断した理由。
     */
    private final String reason;

    /**
     * 例外を生成する。
     *
     * @param itemId       不正な定義を持つアイテムID
     * @param propertyName 不正なプロパティ名
     * @param reason       不正と判断した理由
     */
    public InvalidItemDefinitionException(
            final String itemId,
            final String propertyName,
            final String reason
    ) {
        super(createMessage(
                itemId,
                propertyName,
                reason
        ));

        this.itemId = Objects.requireNonNull(
                itemId,
                "itemId must not be null"
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
     * @param itemId       アイテムID
     * @param propertyName プロパティ名
     * @param reason       不正理由
     * @return 例外メッセージ
     */
    private static String createMessage(
            final String itemId,
            final String propertyName,
            final String reason
    ) {
        return "Invalid item definition"
                + " / itemId="
                + itemId
                + " / property="
                + propertyName
                + " / reason="
                + reason;
    }

    /**
     * 不正な定義を持つアイテムIDを取得する。
     *
     * @return アイテムID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * 不正と判断されたプロパティ名を取得する。
     *
     * @return プロパティ名
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 不正と判断した理由を取得する。
     *
     * @return 不正理由
     */
    public String getReason() {
        return reason;
    }
}