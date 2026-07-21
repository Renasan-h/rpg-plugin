package com.example.rpg.item.exception;

/**
 * 指定されたRPGアイテム定義が存在しない場合に発生する例外。
 *
 * <p>
 * items.ymlに、指定されたitemIdに対応する定義が存在しない場合に使用する。
 * </p>
 */
public class ItemDefinitionNotFoundException extends IllegalArgumentException {
    /**
     * 存在しなかったアイテムID
     */
    private final String itemId;

    /**
     * 例外を生成する。
     *
     * @param itemId 存在しなかったアイテムID
     */
    public ItemDefinitionNotFoundException(
            final String itemId
    ) {
        super(createMessage(itemId));
        this.itemId = itemId;
    }

    /**
     * 例外メッセージを生成する。
     *
     * @param itemId アイテムID
     * @return 例外メッセージ
     */
    private static String createMessage(
            final String itemId
    ) {
        return "Item definition was not found"
                + " / itemId="
                + itemId;
    }
}
