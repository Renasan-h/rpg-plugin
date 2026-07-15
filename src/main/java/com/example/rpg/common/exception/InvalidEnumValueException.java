package com.example.rpg.common.exception;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 設定値を指定されたEnumへ変換できない場合に発生する例外です。
 */
public final class InvalidEnumValueException
        extends ConfigurationException {

    /**
     * Enumへ変換できない設定値の例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 値が不正だったプロパティのパス
     * @param actualValue  実際に設定されていた値
     */
    public InvalidEnumValueException(
            final String itemId,
            final String propertyPath,
            final String actualValue
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                actualValue,
                null
        ));
    }

    /**
     * 許可されたEnum値を含む例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 値が不正だったプロパティのパス
     * @param actualValue  実際に設定されていた値
     * @param enumType     変換対象のEnumクラス
     */
    public InvalidEnumValueException(
            final String itemId,
            final String propertyPath,
            final String actualValue,
            final Class<? extends Enum<?>> enumType
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                actualValue,
                enumType
        ));
    }

    private static String createMessage(
            final String itemId,
            final String propertyPath,
            final String actualValue,
            final Class<? extends Enum<?>> enumType
    ) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        Objects.requireNonNull(
                propertyPath,
                "propertyPath must not be null"
        );
        Objects.requireNonNull(
                actualValue,
                "actualValue must not be null"
        );

        final StringBuilder message = new StringBuilder()
                .append("Invalid enum configuration value")
                .append(" / itemId=")
                .append(itemId)
                .append(" / property=")
                .append(propertyPath)
                .append(" / value=")
                .append(actualValue);

        if (enumType != null) {
            message.append(" / allowedValues=")
                    .append(getAllowedValues(enumType));
        }

        return message.toString();
    }

    private static String getAllowedValues(
            final Class<? extends Enum<?>> enumType
    ) {
        Objects.requireNonNull(enumType, "enumType must not be null");

        return Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}