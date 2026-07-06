package com.example.rpg.dto;

import java.util.Map;

public class ShopDto {

    /**
     * ショップタイトル
     */
    private final String title;
    /**
     * ショップ表示領域
     */
    private final int size;
    /**
     * ショップカテゴリ情報
     */
    private final Map<String, ShopCategoryDto> categories;

    public ShopDto(String title, int size, Map<String, ShopCategoryDto> categories) {
        this.title = title;
        this.size = size;
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public Map<String, ShopCategoryDto> getCategories() {
        return categories;
    }
}
