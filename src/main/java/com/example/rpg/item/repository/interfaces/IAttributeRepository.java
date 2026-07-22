package com.example.rpg.item.repository.interfaces;

import com.example.rpg.common.repository.ReloadableRepository;
import com.example.rpg.item.dto.ItemAttributeDto;

import java.util.Map;

/**
 * RPG プラグイン独自の属性情報用Repository
 */
public interface IAttributeRepository extends ReloadableRepository {
    /**
     * 属性IDから属性定義を取得する。
     *
     * @param attributeId 属性Id
     * @return 属性定義 存在しない場合はnull
     */
    ItemAttributeDto findById(final String attributeId);

    /**
     * 全属性定義を取得する。
     *
     * @return 変更不可能な属性定義一覧
     */
    Map<String, ItemAttributeDto> findAll();


}
