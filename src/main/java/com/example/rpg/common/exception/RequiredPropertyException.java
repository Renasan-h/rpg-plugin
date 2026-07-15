package com.example.rpg.common.exception;

import java.util.Objects;

/**
 * 設定ファイルに必須プロパティが存在しない場合に発生する例外です。
 */
public final class RequiredPropertyException
        extends ConfigurationException {

    /**
     * 必須プロパティ未設定の例外を生成します。
     *
     * @param itemId       読込対象のアイテムID
     * @param propertyPath 未設定だったプロパティのパス
     */
    public RequiredPropertyException(
            final String itemId,
            final String propertyPath
    ) {
        super(createMessage(itemId, propertyPath));
    }

    private static String createMessage(
            final String itemId,
            final String propertyPath
    ) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        Objects.requireNonNull(
                propertyPath,
                "propertyPath must not be null"
        );

        return "Required configuration property is missing"
                + " / itemId="
                + itemId
                + " / property="
                + propertyPath;
    }
}