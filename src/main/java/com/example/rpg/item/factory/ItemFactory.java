package com.example.rpg.item.factory;

import com.example.rpg.item.assembler.interfaces.IItemAssembler;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.exception.InvalidItemAmountException;
import com.example.rpg.item.exception.ItemDefinitionNotFoundException;
import com.example.rpg.item.factory.interfaces.IItemFactory;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * RPGアイテムを生成するFactory。
 *
 * <p>
 * Repositoryからアイテム定義と関連定義を取得し、
 * Bukkit APIで使用可能な値へ変換したうえで、
 * ItemBuilderへItemStackの構築を委譲する。
 * </p>
 *
 * <p>
 * ItemMetaやPersistentDataContainerは直接操作しない。
 * </p>
 */
public final class ItemFactory implements IItemFactory {

    /**
     * アイテム定義Repository。
     */
    private final IItemRepository itemRepository;

    /**
     * ItemStack組み立て処理。
     */
    private final IItemAssembler itemAssembler;


    /**
     * ItemFactoryを生成する。
     *
     * @param itemRepository アイテム定義Repository
     * @param itemAssembler  ItemStack組み立て処理
     * @throws NullPointerException 引数がnullの場合
     */
    public ItemFactory(
            final IItemRepository itemRepository,
            final IItemAssembler itemAssembler
    ) {
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );
        this.itemAssembler = Objects.requireNonNull(
                itemAssembler,
                "itemAssembler must not be null"
        );
    }

    /**
     * アイテムを1個生成する。
     *
     * @param itemId アイテムID
     * @return 生成したItemStack
     */
    @Override
    public ItemStack create(final String itemId) {
        return create(itemId, 1);
    }

    /**
     * 指定個数のアイテムを生成する。
     *
     * @param itemId アイテムID
     * @param amount 生成個数
     * @return 生成したItemStack
     */
    @Override
    public ItemStack create(
            final String itemId,
            final int amount
    ) {
        validateItemId(itemId);

        final ItemDto itemDto = requireItemDefinition(itemId);

        validateAmount(itemDto, amount);

        return itemAssembler.assemble(itemDto, amount);
    }

    /**
     * アイテムIDに対応する定義を取得する。
     *
     * @param itemId アイテムID
     * @return アイテム定義
     * @throws ItemDefinitionNotFoundException アイテム定義が存在しない場合
     */
    private ItemDto requireItemDefinition(final String itemId) {
        final ItemDto itemDto = itemRepository.findById(itemId);

        if (itemDto == null) {
            throw new ItemDefinitionNotFoundException(
                    itemId
            );
        }

        return itemDto;
    }


    /**
     * アイテムIDを検証する。
     *
     * @param itemId アイテムID
     */
    private void validateItemId(final String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException(
                    "itemId must not be null or blank"
            );
        }
    }

    /**
     * 生成個数を検証する。
     *
     * @param itemDto アイテム定義
     * @param amount  生成個数
     * @throws InvalidItemAmountException 生成個数が範囲外の場合
     */
    private void validateAmount(
            final ItemDto itemDto,
            final int amount
    ) {
        final int maxStackSize =
                itemDto.getMaterial().getMaxStackSize();

        if (amount < 1 || amount > maxStackSize) {
            throw new InvalidItemAmountException(
                    itemDto.getId(),
                    amount,
                    maxStackSize
            );
        }
    }
}