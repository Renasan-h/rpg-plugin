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
     * アイテム名
     */
    private final String name;
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
     * アイテムの説明
     */
    private final List<String> lore;
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
     * @param name       名前
     * @param price      買値
     * @param sellPrice  売値
     * @param amount     購入個数
     * @param limit      購入上限
     * @param permission 権限
     * @param lore       説明
     * @param commands   特殊コマンド
     */
    public ShopItemDto(
            String id,
            String itemId,
            int slot,
            ShopItemType type,
            String name,
            int price,
            int sellPrice,
            int amount,
            int limit,
            String permission,
            List<String> lore,
            List<String> commands
    ) {
        this.id = id;
        this.itemId = itemId;
        this.slot = slot;
        this.type = type;
        this.name = name;
        this.price = price;
        this.sellPrice = sellPrice;
        this.amount = amount;
        this.limit = limit;
        this.permission = permission;
        this.lore = lore;
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

    public String getName() {
        return name;
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

    public List<String> getLore() {
        return lore;
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