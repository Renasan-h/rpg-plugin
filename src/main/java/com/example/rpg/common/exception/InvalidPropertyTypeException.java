package com.example.rpg.common.exception;

import java.util.Objects;

/**
 * 設定値の型が期待する型と異なる場合に発生する例外です。
 */
public final class InvalidPropertyTypeException
        extends ConfigurationException {

    /**
     * 設定値の型不正を表す例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 型が不正だったプロパティのパス
     * @param expectedType 期待する型
     */
    public InvalidPropertyTypeException(
            final String itemId,
            final String propertyPath,
            final String expectedType
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                expectedType,
                null
        ));
    }

    /**
     * 実際に取得した値を含む型不正例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 型が不正だったプロパティのパス
     * @param expectedType 期待する型
     * @param actualValue  実際に設定されていた値
     */
    public InvalidPropertyTypeException(
            final String itemId,
            final String propertyPath,
            final String expectedType,
            final Object actualValue
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                expectedType,
                actualValue
        ));
    }

    private static String createMessage(
            final String itemId,
            final String propertyPath,
            final String expectedType,
            final Object actualValue
    ) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        Objects.requireNonNull(
                propertyPath,
                "propertyPath must not be null"
        );
        Objects.requireNonNull(
                expectedType,
                "expectedType must not be null"
        );

        final StringBuilder message = new StringBuilder()
                .append("Invalid configuration property type")
                .append(" / itemId=")
                .append(itemId)
                .append(" / property=")
                .append(propertyPath)
                .append(" / expectedType=")
                .append(expectedType);

        if (actualValue != null) {
            message.append(" / actualType=")
                    .append(actualValue.getClass().getSimpleName())
                    .append(" / actualValue=")
                    .append(actualValue);
        }

        return message.toString();
    }
}