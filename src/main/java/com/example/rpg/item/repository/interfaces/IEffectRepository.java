package com.example.rpg.item.repository.interfaces;

import com.example.rpg.item.dto.ItemEffectDto;

import java.util.Map;

/**
 * RPG プラグイン独自の効果情報用Repository
 */
public interface IEffectRepository {
    /**
     * 効果定義を読み込む。
     *
     * <p>定義が存在しないばあは空</p>
     */
    void load();

    /**
     * 効果IDから効果定義を取得する。
     *
     * @param attributeId 効果Id
     * @return 効果定義 存在しない場合はnull
     */
    ItemEffectDto findById(final String attributeId);

    /**
     * 全効果定義を取得する。
     *
     * @return 変更不可能な効果定義一覧
     */
    Map<String, ItemEffectDto> findAll();
}
