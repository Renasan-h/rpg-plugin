package com.example.rpg.common.exception;

import java.util.Objects;

/**
 * 設定値の型は正しいものの、許可された値の条件に違反している場合に
 * 発生する例外です。
 */
public final class InvalidPropertyValueException
        extends ConfigurationException {

    /**
     * 設定値不正の例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 値が不正だったプロパティのパス
     * @param reason       不正と判断した理由
     */
    public InvalidPropertyValueException(
            final String itemId,
            final String propertyPath,
            final String reason
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                null,
                reason
        ));
    }

    /**
     * 実際に設定された値を含む設定値不正例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 値が不正だったプロパティのパス
     * @param actualValue  実際に設定されていた値
     * @param reason       不正と判断した理由
     */
    public InvalidPropertyValueException(
            final String itemId,
            final String propertyPath,
            final Object actualValue,
            final String reason
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                actualValue,
                reason
        ));
    }

    private static String createMessage(
            final String itemId,
            final String propertyPath,
            final Object actualValue,
            final String reason
    ) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        Objects.requireNonNull(
                propertyPath,
                "propertyPath must not be null"
        );
        Objects.requireNonNull(reason, "reason must not be null");

        final StringBuilder message = new StringBuilder()
                .append("Invalid configuration property value")
                .append(" / itemId=")
                .append(itemId)
                .append(" / property=")
                .append(propertyPath);

        if (actualValue != null) {
            message.append(" / value=")
                    .append(actualValue);
        }

        return message.append(" / reason=")
                .append(reason)
                .toString();
    }
}