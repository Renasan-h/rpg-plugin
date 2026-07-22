package com.example.rpg.item.repository.interfaces;

import com.example.rpg.common.repository.ReloadableRepository;
import com.example.rpg.item.dto.ItemEnchantmentDto;

import java.util.Map;

/**
 * RPG プラグイン独自のエンチャント情報用Repository
 */
public interface IEnchantmentRepository extends ReloadableRepository {
    /**
     * エンチャントIDからエンチャント定義を取得する。
     *
     * @param enchantId エンチャントID
     * @return エンチャント定義 存在しない場合はnull
     */
    ItemEnchantmentDto findById(final String enchantId);

    /**
     * 全エンチャント定義を取得する。
     *
     * @return 変更不可能なエンチャント定義一覧
     */
    Map<String, ItemEnchantmentDto> findAll();
}
