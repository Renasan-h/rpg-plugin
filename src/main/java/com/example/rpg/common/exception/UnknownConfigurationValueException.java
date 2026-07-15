package com.example.rpg.common.exception;

import java.util.Objects;

/**
 * Material、Attribute、Enchantmentなど、設定で指定された名前に対応する
 * 登録済みオブジェクトが存在しない場合に発生する例外です。
 */
public final class UnknownConfigurationValueException
        extends ConfigurationException {

    /**
     * 未知の設定値を表す例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 未知の値が指定されたプロパティのパス
     * @param actualValue  実際に設定されていた値
     */
    public UnknownConfigurationValueException(
            final String itemId,
            final String propertyPath,
            final String actualValue
    ) {
        super(createMessage(
                itemId,
                propertyPath,
                actualValue
        ));
    }

    private static String createMessage(
            final String itemId,
            final String propertyPath,
            final String actualValue
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

        return "Unknown configuration value"
                + " / itemId="
                + itemId
                + " / property="
                + propertyPath
                + " / value="
                + actualValue;
    }
}