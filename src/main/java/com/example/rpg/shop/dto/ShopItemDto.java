package com.example.rpg.shop.dto;

import java.util.List;

/**
 * ショップアイテムのDTO
 */
public class ShopItemDto {

    /**
     * キー
     */
    private final String id;
    /**
     * アイテムID
     */
    private final String itemId;
    /**
     * 表示箇所スロット番号
     */
    private final int slot;
    /**
     * タイプ（ITEM or COMMAND）
     */
    private final ShopItemType type;
    /**
     * 買値
     */
    private final int price;
    /**
     * 売値
     */
    private final int sellPrice;
    /**
     * 購入個数
     */
    private final int amount;
    /**
     * アイテム購入上限
     */
    private final int limit;
    /**
     * アイテム購入に必要な権限
     */
    private final String permission;
    /**
     * アイテムの特殊コマンド
     */
    private final List<String> commands;

    /**
     *
     * @param id         キー
     * @param itemId     アイテムID
     * @param slot       スロット番号
     * @param type       タイプ
     * @param price      買値
     * @param sellPrice  売値
     * @param amount     購入個数
     * @param limit      購入上限
     * @param permission 権限
     * @param commands   特殊コマンド
     */
    public ShopItemDto(
            String id,
            String itemId,
            int slot,
            ShopItemType type,
            int price,
            int sellPrice,
            int amount,
            int limit,
            String permission,
            List<String> commands
    ) {
        this.id = id;
        this.itemId = itemId;
        this.slot = slot;
        this.type = type;
        this.price = price;
        this.sellPrice = sellPrice;
        this.amount = amount;
        this.limit = limit;
        this.permission = permission;
        this.commands = commands;
    }

    public String getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public int getSlot() {
        return slot;
    }

    public ShopItemType getType() {
        return type;
    }

    public int getPrice() {
        return price;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public int getAmount() {
        return amount;
    }

    public int getLimit() {
        return limit;
    }

    public String getPermission() {
        return permission;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isCommandItem() {
        return type == ShopItemType.COMMAND;
    }

    public boolean isSellable() {
        return type == ShopItemType.ITEM && sellPrice > 0;
    }
}