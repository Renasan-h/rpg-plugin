package com.example.rpg.item;

import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEnchantDto;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;

/**
 * RPGアイテム定義からItemStackを生成するBuilder。
 *
 * <p>
 * ItemStack・ItemMeta・PDCの生成処理をこのクラスへ集約し、
 * 呼び出し側での直接生成を防止する。
 * </p>
 */
public class ItemBuilder {
    /**
     * Bukkit ItemStackの最小個数
     */
    private static final int MIN_ITEM_AMOUNT = 1;

    /**
     * アイテム定義Repository
     */
    private final IItemRepository itemRepository;

    /**
     * RPGアイテム用PDCキー
     */
    private final ItemPdcKeys pdcKeys;

    /**
     * ItemBuilderを生成する。
     *
     * @param itemRepository アイテム定義Repository
     * @param pdcKeys        RPGアイテム用PDCキー
     */
    public ItemBuilder(
            final IItemRepository itemRepository,
            final ItemPdcKeys pdcKeys
    ) {
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );

        this.pdcKeys = Objects.requireNonNull(
                pdcKeys,
                "pdcKeys must not be null"
        );
    }

    /**
     * 指定されたアイテムIDからItemStackを生成する。
     *
     * @param itemId アイテムID
     * @return 生成したItemStack
     * @throws IllegalArgumentException アイテム定義が存在しない場合
     */
    public ItemStack build(final String itemId) {
        return build(itemId, MIN_ITEM_AMOUNT);
    }

    /**
     * 指定されたアイテムIDと個数からItemStackを生成する。
     *
     * @param itemId アイテムID
     * @param amount 生成個数
     * @return 生成したItemStack
     * @throws IllegalArgumentException アイテム定義が存在しない場合、または個数が不正な場合
     */
    public ItemStack build(
            final String itemId,
            final int amount
    ) {
        if (amount < MIN_ITEM_AMOUNT) {
            throw new IllegalArgumentException(
                    "amount must greater than 0:" + amount
            );
        }

        final ItemDto item = itemRepository.findById(itemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Item definition was not found: " + itemId
            );
        }

        if (amount > item.getMaterial().getMaxStackSize()) {
            throw new IllegalArgumentException(
                    "amount exceeds max stack size: "
                            + amount
                            + " / itemId="
                            + itemId
            );
        }

        return buildItemStack(item, amount);
    }

    /**
     * ItemDtoからItemStackを生成する。
     *
     * <p>
     * new ItemStackを使用するのは、このメソッドを含む
     * ItemBuilder内部のみに限定する。
     * </p>
     *
     * @param item   アイテム定義
     * @param amount 生成個数
     * @return 生成したItemStack
     */
    private ItemStack buildItemStack(
            final ItemDto item,
            final int amount
    ) {
        final ItemStack itemStack = new ItemStack(item.getMaterial(), amount);

        final ItemMeta meta = itemStack.getItemMeta();

        applyDisplayName(meta, item);
        applyLore(meta, item);
        applyItemFlags(meta, item);
        applyUnbreakable(meta, item);
        applyCustomModelData(meta, item);
        applyEnchantments(meta, item);
        applyPersistentData(meta, item);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * アイテム表示名を設定する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyDisplayName(
            final ItemMeta meta,
            final ItemDto item
    ) {
        meta.displayName(MessageUtil.mm(item.getDisplayName()));
    }

    /**
     * アイテムLoreを設定する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyLore(
            final ItemMeta meta,
            final ItemDto item
    ) {
        final List<Component> lore = item.getLore()
                .stream()
                .map(MessageUtil::mm)
                .toList();

        meta.lore(lore);
    }

    /**
     * アイテム定義に設定されたItemFlagを適用する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyItemFlags(
            final ItemMeta meta,
            final ItemDto item
    ) {
        if (item.getItemFlags().isEmpty()) {
            return;
        }

        meta.addItemFlags(
                item.getItemFlags()
                        .toArray(ItemFlag[]::new)
        );
    }

    /**
     * RPGアイテム識別用のPDCを設定する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyPersistentData(
            final ItemMeta meta,
            final ItemDto item
    ) {
        meta.getPersistentDataContainer().set(
                pdcKeys.getItemIdKey(),
                PersistentDataType.STRING,
                item.getId()
        );
    }

    /**
     * アイテムのUnbreakable設定を適用する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyUnbreakable(
            final ItemMeta meta,
            final ItemDto item
    ) {
        meta.setUnbreakable(item.isUnbreakable());
    }

    /**
     * アイテム定義のCustomModelDataを適用する。
     *
     * <p>
     * ItemMetaから取得したCustomModelDataComponentはスナップショットのため、
     * 値を変更した後にItemMetaへ再設定する。
     * </p>
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    @SuppressWarnings("deprecation")
    private void applyCustomModelData(
            final ItemMeta meta,
            final ItemDto item
    ) {
        final Integer customModelData = item.getCustomModelData();

        if (customModelData == null) {
            return;
        }

        meta.setCustomModelData(customModelData);
    }

    /**
     * アイテム定義のエンチャントを適用する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyEnchantments(
            final ItemMeta meta,
            final ItemDto item
    ) {
        for (ItemEnchantDto enchant : item.getEnchantments()) {
            meta.addEnchant(enchant.getEnchantment(),
                    enchant.getLevel(),
                    enchant.isIgnoreLevelRestriction()
            );
        }
    }
}
