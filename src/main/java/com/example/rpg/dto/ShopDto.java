package com.example.rpg.dto;

import java.util.Map;

public class ShopDto {
    private final String title;
    private final int size;
    private final Map<String, ShopItemDto> items;

    public ShopDto(String title, int size, Map<String, ShopItemDto> items) {
        this.title = title;
        this.size = size;
        this.items = items;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public Map<String, ShopItemDto> getItems() {
        return items;
    }
}
