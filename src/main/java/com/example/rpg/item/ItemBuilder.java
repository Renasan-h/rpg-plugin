package com.example.rpg.item;

import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.dto.ItemAttributeDto;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEffectDto;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
     * 属性定義Repository
     */
    private final IAttributeRepository attributeRepository;

    /**
     * エンチャント定義Repository
     */
    private final IEnchantmentRepository enchantmentRepository;

    /**
     * 効果定義Repository
     */
    private final IEffectRepository effectRepository;

    /**
     * RPGアイテム用PDCキー
     */
    private final ItemPdcKeys pdcKeys;

    /**
     * NamespacedKey生成に使用するプラグインインスタンス。
     */
    private final JavaPlugin plugin;

    /**
     * ItemBuilderを生成する。
     *
     * @param itemRepository        アイテム定義Repository
     * @param attributeRepository   属性定義Repository
     * @param enchantmentRepository エンチャント定義Repository
     * @param effectRepository      効果定義Repository
     * @param pdcKeys               RPGアイテム用PDCキー
     * @param plugin                プラグインインスタンス
     */
    public ItemBuilder(
            final IItemRepository itemRepository,
            final IAttributeRepository attributeRepository,
            final IEnchantmentRepository enchantmentRepository,
            final IEffectRepository effectRepository,
            final ItemPdcKeys pdcKeys,
            final JavaPlugin plugin
    ) {
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );

        this.attributeRepository = Objects.requireNonNull(
                attributeRepository,
                "attributeRepository must not be null"
        );

        this.enchantmentRepository = Objects.requireNonNull(
                enchantmentRepository,
                "enchantmentRepository must not be null"
        );

        this.effectRepository = Objects.requireNonNull(
                effectRepository,
                "effectRepository must not be null"
        );

        this.pdcKeys = Objects.requireNonNull(
                pdcKeys,
                "pdcKeys must not be null"
        );

        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin must not be null"
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

        // アイテムの構成情報を適用する
        applyDisplayName(meta, item);
        applyLore(meta, item);
        applyItemFlags(meta, item);
        applyUnbreakable(meta, item);
        applyCustomModelData(meta, item);
        applyEnchantments(meta, item);
        applyAttributes(meta, item);
        applyEffects(meta, item);
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
     * アイテム定義のエンチャントIDをもとに、エンチャント一覧Repositoryから値を検索し適用する。
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     * @throws InvalidPropertyValueException 不正なエンチャント名
     */
    private void applyEnchantments(
            final ItemMeta meta,
            final ItemDto item
    ) {
        int errCnt = 0;
        List<String> errEnchantKeyList = new ArrayList<>();
        for (String enchantKey : item.getEnchantments()) {
            ItemEnchantmentDto enchantment = enchantmentRepository.findById(enchantKey);
            if (enchantment == null) {
                // エンチャントの紐づきがない場合は不正だが
                // 一括で出力するために不正エンチャントをリストへ格納
                errEnchantKeyList.add(enchantKey);
                errCnt++;
                continue;
            }
            meta.addEnchant(enchantment.getEnchantment(),
                    enchantment.getLevel(),
                    enchantment.isIgnoreLevelRestriction()
            );
        }
        if (errCnt > 0) {
            // 不正エンチャントが1つ以上あれば表示する
            throw new InvalidPropertyValueException(
                    item.getId(),
                    String.join(",", errEnchantKeyList),
                    "invalid Enchant Name"
            );
        }
    }

    /**
     * アイテム定義に含まれるAttributeModifierをItemMetaへ適用する。
     *
     * <p>
     * AttributeModifierの識別キーはアイテムIDとAttributeの連番から生成し、
     * 同じアイテム定義では常に同一のキーを使用する。
     * </p>
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyAttributes(
            final ItemMeta meta,
            final ItemDto item
    ) {
        // attributeIDのリスト
        final List<String> attributes = item.getAttributes();
        int errCnt = 0;
        List<String> errAttributeList = new ArrayList<>();

        for (int index = 0; index < attributes.size(); index++) {
            // final ItemAttributeDto attribute = attributes.get(index);
            final ItemAttributeDto attribute = attributeRepository.findById(attributes.get(index));

            if (attribute == null) {
                // エンチャントの紐づきがない場合は不正だが
                // 一括で出力するために不正エンチャントをリストへ格納
                errAttributeList.add(attributes.get(index));
                errCnt++;
                continue;
            }

            // item/xxxxxx/attribute/[index] の形式で作成する
            final AttributeModifier modifier = getAttributeModifier(item, index, attribute);

            meta.addAttributeModifier(
                    attribute.getAttribute(),
                    modifier);
        }

        if (errCnt > 0) {
            // 不正エンチャントが1つ以上あれば表示する
            throw new InvalidPropertyValueException(
                    item.getId(),
                    String.join(",", errAttributeList),
                    "invalid Attribute Name"
            );
        }
    }

    /**
     * AttributeModifierを作成する。
     *
     * @param item      Item
     * @param index     Attributes Index
     * @param attribute ItemAttribute
     * @return AttributeModifier
     */
    private @NonNull AttributeModifier getAttributeModifier(
            final ItemDto item,
            final int index,
            final ItemAttributeDto attribute
    ) {
        final NamespacedKey modifierKey =
                new NamespacedKey(
                        plugin,
                        "item/"
                                + item.getId().toLowerCase(Locale.ROOT)
                                + "/attribute/"
                                + index
                );

        return new AttributeModifier(
                modifierKey,
                attribute.getAmount(),
                attribute.getOperation(),
                attribute.getSlotGroup()
        );
    }

    /**
     * アイテムへPotionEffectを適用する。
     *
     * <p>
     * PotionMetaを持つアイテムのみPotionEffectを設定する。
     * </p>
     *
     * @param meta ItemMeta
     * @param item アイテム定義
     */
    private void applyEffects(
            final ItemMeta meta,
            final ItemDto item
    ) {
        if (!(meta instanceof PotionMeta potionMeta)) {
            return;
        }

        int errCnt = 0;
        List<String> errEffectList = new ArrayList<>();

        for (String effectId : item.getEffects()) {
            final ItemEffectDto effect = effectRepository.findById(effectId);

            if (effect == null) {
                errEffectList.add(effectId);
                errCnt++;
                continue;
            }

            final PotionEffect potionEffect =
                    new PotionEffect(
                            effect.getEffectType(),
                            effect.getDuration(),
                            effect.getAmplifier(),
                            effect.isAmbient(),
                            effect.isParticles(),
                            effect.isIcon()
                    );

            potionMeta.addCustomEffect(
                    potionEffect,
                    true
            );
        }

        if (errCnt > 0) {
            // 不正エンチャントが1つ以上あれば表示する
            throw new InvalidPropertyValueException(
                    item.getId(),
                    String.join(",", errEffectList),
                    "invalid Potion Effect Name"
            );
        }


    }
}
