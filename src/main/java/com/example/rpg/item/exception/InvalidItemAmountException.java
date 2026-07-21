package com.example.rpg.item.exception;

/**
 * RPGアイテムの生成個数が不正な場合に発生する例外。
 */
public class InvalidItemAmountException extends IllegalArgumentException {

    /**
     * 対象アイテムID。
     */
    private final String itemId;

    /**
     * 指定された生成個数。
     */
    private final int amount;

    /**
     * 生成可能な最大個数。
     */
    private final int maxStackSize;

    /**
     * 例外を生成する。
     *
     * @param itemId       対象アイテムID
     * @param amount       指定された生成個数
     * @param maxStackSize 最大スタック数
     */
    public InvalidItemAmountException(
            final String itemId,
            final int amount,
            final int maxStackSize
    ) {
        super(createMessage(
                itemId,
                amount,
                maxStackSize
        ));

        this.itemId = itemId;
        this.amount = amount;
        this.maxStackSize = maxStackSize;
    }

    /**
     * 例外メッセージを生成する。
     *
     * @param itemId       アイテムID
     * @param amount       指定個数
     * @param maxStackSize 最大スタック数
     * @return 例外メッセージ
     */
    private static String createMessage(
            final String itemId,
            final int amount,
            final int maxStackSize
    ) {
        return "Item amount is out of range"
                + " / itemId="
                + itemId
                + " / amount="
                + amount
                + " / allowedRange=1-"
                + maxStackSize;
    }

    /**
     * 対象アイテムIDを取得する。
     *
     * @return アイテムID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * 指定された生成個数を取得する。
     *
     * @return 生成個数
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 最大スタック数を取得する。
     *
     * @return 最大スタック数
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }
}
