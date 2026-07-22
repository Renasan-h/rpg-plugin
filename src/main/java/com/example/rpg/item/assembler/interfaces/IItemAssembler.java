package com.example.rpg.item.assembler.interfaces;

import com.example.rpg.item.dto.ItemDto;
import org.bukkit.inventory.ItemStack;

/**
 * RPGアイテム定義からItemStackを組み立てるAssembler。
 *
 * <p>
 * ItemDtoおよび関連する定義をBukkit APIで使用可能な値へ変換し、
 * ItemStackを生成する責務を持つ。
 * </p>
 */
public interface IItemAssembler {

    /**
     * アイテム定義からItemStackを生成する
     *
     * @param itemDto アイテム定義
     * @param amount  生成個数
     * @return 生成ItemStack
     * @throws NullPointerException itemDtoがnull
     */
    ItemStack assemble(ItemDto itemDto, int amount);
}
