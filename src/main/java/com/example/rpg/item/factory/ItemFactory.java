package com.example.rpg.item.factory;

import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.item.builder.ItemBuilder;
import com.example.rpg.item.dto.ItemAttributeDto;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEffectDto;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.factory.interfaces.IItemFactory;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.repository.interfaces.IItemRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
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
     * エンチャント定義Repository。
     */
    private final IEnchantmentRepository enchantmentRepository;

    /**
     * 属性定義Repository。
     */
    private final IAttributeRepository attributeRepository;

    /**
     * 効果定義Repository。
     */
    private final IEffectRepository effectRepository;

    /**
     * MiniMessageインスタンス。
     */
    private final MiniMessage miniMessage;

    /**
     * RPGアイテム用PDCキー。
     */
    private final ItemPdcKeys itemPdcKeys;

    /**
     * プラグインインスタンス
     */
    private final JavaPlugin plugin;

    /**
     * ItemFactoryを生成する。
     *
     * @param itemRepository        アイテム定義Repository
     * @param enchantmentRepository エンチャント定義Repository
     * @param attributeRepository   属亭定義Repository
     * @param effectRepository      効果定義Repository
     * @param miniMessage           MiniMessage インスタンス
     * @param itemPdcKeys           RPGアイテム用PDCキー
     * @param plugin                プラグインインスタンス
     * @throws NullPointerException 引数がnullの場合
     */
    public ItemFactory(
            final IItemRepository itemRepository,
            final IEnchantmentRepository enchantmentRepository,
            final IAttributeRepository attributeRepository,
            final IEffectRepository effectRepository,
            final MiniMessage miniMessage,
            final ItemPdcKeys itemPdcKeys,
            final JavaPlugin plugin
    ) {
        this.itemRepository = Objects.requireNonNull(
                itemRepository,
                "itemRepository must not be null"
        );
        this.enchantmentRepository = Objects.requireNonNull(
                enchantmentRepository,
                "enchantmentRepository must not be null"
        );
        this.attributeRepository = Objects.requireNonNull(
                attributeRepository,
                "attributeRepository must not be null"
        );
        this.effectRepository = Objects.requireNonNull(
                effectRepository,
                "effectRepository must not be null"
        );
        this.miniMessage = Objects.requireNonNull(
                miniMessage,
                "miniMessage must not be null"
        );
        this.itemPdcKeys = Objects.requireNonNull(
                itemPdcKeys,
                "itemPdcKeys must not be null"
        );
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin must not be null"
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

        return buildItemStack(itemDto, amount);
    }

    /**
     * アイテムIDに対応する定義を取得する。
     *
     * @param itemId アイテムID
     * @return アイテム定義
     * @throws IllegalArgumentException 定義が存在しない場合
     */
    private ItemDto requireItemDefinition(final String itemId) {
        final ItemDto itemDto = itemRepository.findById(itemId);

        if (itemDto == null) {
            throw new IllegalArgumentException(
                    "アイテム定義が存在しません: itemId=" + itemId
            );
        }

        return itemDto;
    }

    /**
     * ItemDtoからItemStackを生成する。
     *
     * @param itemDto アイテム定義
     * @param amount  生成個数
     * @return 生成したItemStack
     */
    private ItemStack buildItemStack(
            final ItemDto itemDto,
            final int amount
    ) {
        final ItemBuilder builder =
                ItemBuilder.builder(itemPdcKeys)
                        .material(itemDto.getMaterial())
                        .amount(amount)
                        .displayName(
                                deserialize(itemDto.getDisplayName())
                        )
                        .lore(
                                deserializeLore(itemDto.getLore())
                        )
                        .itemFlags(itemDto.getItemFlags())
                        .unbreakable(itemDto.isUnbreakable())
                        .customModelData(
                                itemDto.getCustomModelData()
                        )
                        .itemId(itemDto.getId());

        applyEnchantments(builder, itemDto);
        applyAttributes(builder, itemDto);
        applyEffects(builder, itemDto);

        return builder.build();
    }

    /**
     * アイテム定義に設定されたエンチャントをBuilderへ渡す。
     *
     * @param builder ItemBuilder
     * @param itemDto アイテム定義
     * @throws InvalidPropertyValueException エンチャントIDに対応する定義が存在しない場合
     */
    private void applyEnchantments(
            final ItemBuilder builder,
            final ItemDto itemDto
    ) {
        for (String enchantmentId : itemDto.getEnchantments()) {
            final ItemEnchantmentDto enchantment =
                    requireEnchantmentDefinition(
                            itemDto.getId(),
                            enchantmentId
                    );

            builder.enchantment(
                    enchantment.getEnchantment(),
                    enchantment.getLevel(),
                    enchantment.isIgnoreLevelRestriction()
            );
        }
    }

    /**
     * アイテム定義に設定された付与属性をBuilderへ渡す。
     *
     * @param builder ItemBuilder
     * @param itemDto アイテム定義
     * @throws InvalidPropertyValueException 属性IDに対応する定義が存在しない場合
     */
    private void applyAttributes(
            final ItemBuilder builder,
            final ItemDto itemDto
    ) {
        List<String> attributes = itemDto.getAttributes();
        for (int index = 0; index < attributes.size(); index++) {
            final ItemAttributeDto attribute =
                    requireAttributeDefinition(
                            itemDto.getId(),
                            itemDto.getAttributes().get(index)
                    );

            AttributeModifier attributeModifier = getAttributeModifier(
                    itemDto.getId(),
                    index,
                    attribute
            );

            builder.attribute(
                    attribute.getAttribute(),
                    attributeModifier
            );
        }
    }

    /**
     * AttributeModifierを作成する。
     *
     * @param itemId    アイテムID
     * @param index     Attributes Index
     * @param attribute ItemAttribute
     * @return AttributeModifier
     */
    private @NonNull AttributeModifier getAttributeModifier(
            final String itemId,
            final int index,
            final ItemAttributeDto attribute
    ) {
        final NamespacedKey modifierKey =
                new NamespacedKey(
                        plugin,
                        createAttributeModifierKey(
                                itemId,
                                index
                        )
                );

        return new AttributeModifier(
                modifierKey,
                attribute.getAmount(),
                attribute.getOperation(),
                attribute.getSlotGroup()
        );
    }

    /**
     * AttributeModifier用NamespacedKeyのキー部分を生成する。
     *
     * @param itemId アイテムID
     * @param index  Attributeの指定順
     * @return NamespacedKeyのキー部分
     */
    private String createAttributeModifierKey(
            final String itemId,
            final int index
    ) {
        return "item/"
                + itemId.toLowerCase(Locale.ROOT)
                + "/attribute/"
                + index;
    }

    /**
     * アイテム定義に設定されたポーションエフェクトをBuilderへ渡す。
     *
     * @param builder ItemBuilder
     * @param itemDto アイテム定義
     * @throws InvalidPropertyValueException 効果IDに対応する定義が存在しない場合
     */
    private void applyEffects(
            final ItemBuilder builder,
            final ItemDto itemDto
    ) {
        for (String effectId : itemDto.getEffects()) {
            final ItemEffectDto effect =
                    requireEffectDefinition(
                            itemDto.getId(),
                            effectId
                    );

            builder.effect(createPotionEffect(effect));
        }
    }

    /**
     * PotionEffect定義からPotionEffectを生成する。
     *
     * @param effect PotionEffect定義
     * @return PotionEffect
     */
    private PotionEffect createPotionEffect(
            final ItemEffectDto effect
    ) {
        return new PotionEffect(
                effect.getEffectType(),
                effect.getDuration(),
                effect.getAmplifier(),
                effect.isAmbient(),
                effect.isParticles(),
                effect.isIcon()
        );
    }

    /**
     * エンチャント定義を取得する。
     *
     * @param itemId        アイテムID
     * @param enchantmentId エンチャントID
     * @return エンチャント定義
     * @throws InvalidPropertyValueException エンチャント定義が存在しない場合
     */
    private ItemEnchantmentDto requireEnchantmentDefinition(
            final String itemId,
            final String enchantmentId
    ) {
        final ItemEnchantmentDto enchantment =
                enchantmentRepository.findById(enchantmentId);

        if (enchantment == null) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "enchantments",
                    enchantmentId,
                    "Referenced enchantment definition does not exist"
            );
        }

        return enchantment;
    }

    /**
     * 属性定義を取得する。
     *
     * @param attributeId 属性ID
     * @return 属性定義
     * @throws InvalidPropertyValueException 属性定義が存在しない場合
     */
    private ItemAttributeDto requireAttributeDefinition(
            final String itemId,
            final String attributeId
    ) {
        final ItemAttributeDto attribute =
                attributeRepository.findById(attributeId);

        if (attribute == null) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "attributes",
                    attributeId,
                    "Referenced attribute definition does not exist"
            );
        }

        return attribute;
    }

    /**
     * 属性定義を取得する。
     *
     * @param itemId   アイテムID
     * @param effectId 効果ID
     * @return 効果定義
     * @throws InvalidPropertyValueException 効果定義が存在しない場合
     */
    private ItemEffectDto requireEffectDefinition(
            final String itemId,
            final String effectId
    ) {
        final ItemEffectDto effect =
                effectRepository.findById(effectId);

        if (effect == null) {
            throw new InvalidPropertyValueException(
                    itemId,
                    "effects",
                    effectId,
                    "Referenced effects definition does not exist"
            );
        }

        return effect;
    }

    /**
     * MiniMessage形式の文字列をComponentへ変換する。
     *
     * @param value MiniMessage形式の文字列
     * @return Component
     */
    private Component deserialize(final String value) {
        return miniMessage.deserialize(value);
    }

    /**
     * MiniMessage形式のLoreをComponent一覧へ変換する。
     *
     * @param lore MiniMessage形式のLore一覧
     * @return Component一覧
     */
    private List<Component> deserializeLore(
            final List<String> lore
    ) {
        return lore.stream()
                .map(this::deserialize)
                .toList();
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
     */
    private void validateAmount(
            final ItemDto itemDto,
            final int amount
    ) {
        final int maxStackSize =
                itemDto.getMaterial().getMaxStackSize();

        if (amount < 1 || amount > maxStackSize) {
            throw new IllegalArgumentException(
                    "amount is out of range"
                            + " / amount="
                            + amount
                            + " / maxStackSize="
                            + maxStackSize
                            + " / itemId="
                            + itemDto.getId()
            );
        }
    }
}