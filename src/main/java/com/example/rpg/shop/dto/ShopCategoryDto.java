package com.example.rpg.shop.dto;

import org.bukkit.Material;

import java.util.Map;

public class ShopCategoryDto {

    /**
     * キー
     */
    private final String id;
    /**
     * カテゴリ名
     */
    private final String name;
    /**
     * 表示箇所用スロット番号
     */
    private final int slot;
    /**
     * 材質
     */
    private final Material icon;
    /**
     * アイテム情報
     */
    private final Map<String, ShopItemDto> items;

    public ShopCategoryDto(String id, String name, int slot, Material icon, Map<String, ShopItemDto> items) {
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.icon = icon;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSlot() {
        return slot;
    }

    public Material getIcon() {
        return icon;
    }

    public Map<String, ShopItemDto> getItems() {
        return items;
    }
}
