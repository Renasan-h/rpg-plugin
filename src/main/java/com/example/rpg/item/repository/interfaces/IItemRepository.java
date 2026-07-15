package com.example.rpg.item.repository.interfaces;

import com.example.rpg.item.dto.ItemDto;

import java.util.List;

/**
 * RPGアイテム定義を管理するRepository
 *
 * <p>
 * アイテム定義の保存形式を呼び出し側から隠蔽し、
 * YAMLやPostgreSQLなどの実装差分をRepository内部へ閉じ込める。
 * </p>
 */
public interface IItemRepository {

    /**
     * アイテム定義を読み込む。
     */
    void load();

    /**
     * アイテムIDから定義を取得する。
     *
     * @param itemId アイテムID
     * @return アイテム定義 存在しない場合はnull
     */
    ItemDto findById(String itemId);

    /**
     * 全アイテム定義を取得する。
     *
     * @return 変更不可能なアイテム定義一覧
     */
    List<ItemDto> findAll();
}
