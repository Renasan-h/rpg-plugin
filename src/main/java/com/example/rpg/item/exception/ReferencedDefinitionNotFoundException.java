package com.example.rpg.item.exception;

import java.util.Objects;

/**
 * RPGアイテムが参照する関連定義が存在しない場合に発生する例外。
 *
 * <p>
 * アイテム定義に指定されたEnchantment、Attribute、EffectなどのIDが、
 * 対応する定義ファイルに存在しない場合に使用する。
 * </p>
 */
public class ReferencedDefinitionNotFoundException extends IllegalArgumentException {

    /**
     * 参照元アイテムID
     */
    private final String itemId;

    /**
     * 参照対象プロパティ名。
     */
    private final String propertyName;

    /**
     * 存在しなかった参照ID。
     */
    private final String referencedId;

    /**
     * 例外を生成する。
     *
     * @param itemId       参照元アイテムID
     * @param propertyName 参照対象プロパティ名
     * @param referencedId 存在しなかった参照ID
     */
    public ReferencedDefinitionNotFoundException(
            final String itemId,
            final String propertyName,
            final String referencedId
    ) {
        super(createMessage(
                itemId,
                propertyName,
                referencedId
        ));

        this.itemId = Objects.requireNonNull(
                itemId,
                "itemId must not be null"
        );
        this.propertyName = Objects.requireNonNull(
                propertyName,
                "propertyName must not be null"
        );
        this.referencedId = Objects.requireNonNull(
                referencedId,
                "referencedId must not be null"
        );
    }

    /**
     * 例外メッセージを生成する。
     *
     * @param itemId       参照元アイテムID
     * @param propertyName 参照対象プロパティ名
     * @param referencedId 存在しなかった参照ID
     * @return 例外メッセージ
     */
    private static String createMessage(
            final String itemId,
            final String propertyName,
            final String referencedId
    ) {
        return "Referenced definition was not found"
                + " / itemId="
                + itemId
                + " / property="
                + propertyName
                + " / referencedId="
                + referencedId;
    }

    /**
     * 参照元アイテムIDを取得する。
     *
     * @return 参照元アイテムID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * 参照対象プロパティ名を取得する。
     *
     * @return プロパティ名
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 存在しなかった参照IDを取得する。
     *
     * @return 参照ID
     */
    public String getReferencedId() {
        return referencedId;
    }
}
