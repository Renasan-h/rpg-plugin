package com.example.rpg.item.factory.interfaces;

import com.example.rpg.item.exception.InvalidItemAmountException;
import com.example.rpg.item.exception.ItemDefinitionNotFoundException;
import com.example.rpg.item.exception.ReferencedDefinitionNotFoundException;
import org.bukkit.inventory.ItemStack;

/**
 * RPGアイテム生成Factoryのインターフェース。
 */
public interface IItemFactory {

    /**
     * 指定されたアイテムを1個生成する。
     *
     * @param itemId アイテムID
     * @return 生成したItemStack
     * @throws IllegalArgumentException              itemIdがnullまたは空文字の場合
     * @throws ItemDefinitionNotFoundException       アイテム定義が存在しない場合
     * @throws ReferencedDefinitionNotFoundException 関連定義が存在しない場合
     */
    ItemStack create(String itemId);

    /**
     * 指定されたアイテムを指定個数生成する。
     *
     * @param itemId アイテムID
     * @param amount 生成個数
     * @return 生成したItemStack
     * @throws IllegalArgumentException              itemIdがnullまたは空文字の場合
     * @throws ItemDefinitionNotFoundException       アイテム定義が存在しない場合
     * @throws InvalidItemAmountException            生成個数が範囲外の場合
     * @throws ReferencedDefinitionNotFoundException 関連定義が存在しない場合
     */
    ItemStack create(String itemId, int amount);
}