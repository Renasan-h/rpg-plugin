package com.example.rpg.item.assembler;

import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.message.MessageUtil;
import com.example.rpg.item.assembler.interfaces.IItemAssembler;
import com.example.rpg.item.builder.ItemBuilder;
import com.example.rpg.item.dto.ItemAttributeDto;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEffectDto;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.exception.ReferencedDefinitionNotFoundException;
import com.example.rpg.item.pdc.ItemPdcKeys;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import net.kyori.adventure.text.Component;
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
 * RPGアイテム定義からItemStackを組み立てるAssembler。
 *
 * <p>
 * ItemDtoが参照しているエンチャント、属性および効果の定義を解決し、
 * Bukkit APIで使用可能な値へ変換する。
 * </p>
 *
 * <p>
 * ItemMetaへの具体的な設定処理はItemBuilderへ委譲する。
 * </p>
 */
public class ItemAssembler implements IItemAssembler {

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
     * RPGアイテム用PDCキー。
     */
    private final ItemPdcKeys itemPdcKeys;

    /**
     * プラグインインスタンス
     */
    private final JavaPlugin plugin;

    /**
     * ItemAssemblerを生成する。
     *
     * @param enchantmentRepository エンチャント定義Repository
     * @param attributeRepository   属性定義Repository
     * @param effectRepository      効果定義Repository
     * @param itemPdcKeys           RPGアイテム用PDCキー
     * @param plugin                プラグインインスタンス
     * @throws NullPointerException 引数がnullの場合
     */
    public ItemAssembler(
            final IEnchantmentRepository enchantmentRepository,
            final IAttributeRepository attributeRepository,
            final IEffectRepository effectRepository,
            final ItemPdcKeys itemPdcKeys,
            final JavaPlugin plugin
    ) {
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
     * アイテム定義からItemStackを組み立てる。
     *
     * @param itemDto アイテム定義
     * @param amount  生成個数
     * @return 組み立てたItemStack
     * @throws NullPointerException itemDtoがnullの場合
     */
    @Override
    public ItemStack assemble(
            final ItemDto itemDto,
            final int amount
    ) {
        Objects.requireNonNull(
                itemDto,
                "itemDto must not be null"
        );

        final ItemBuilder builder =
                ItemBuilder.builder(itemPdcKeys)
                        .material(itemDto.getMaterial())
                        .amount(amount)
                        .displayName(deserialize(itemDto.getDisplayName()))
                        .lore(deserializeLore(itemDto.getLore()))
                        .itemFlags(itemDto.getItemFlags())
                        .unbreakable(itemDto.isUnbreakable())
                        .customModelData(itemDto.getCustomModelData())
                        .itemId(itemDto.getId());

        applyAttributes(builder, itemDto);
        applyEnchantments(builder, itemDto);
        applyEffects(builder, itemDto);

        return builder.build();
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
        final List<String> attributes = itemDto.getAttributes();

        for (int index = 0; index < attributes.size(); index++) {
            final ItemAttributeDto attribute =
                    requireAttributeDefinition(
                            itemDto.getId(),
                            itemDto.getAttributes().get(index)
                    );

            AttributeModifier attributeModifier = createAttributeModifier(
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
    private @NonNull AttributeModifier createAttributeModifier(
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
     * @param itemId        参照元アイテムID
     * @param enchantmentId エンチャントID
     * @return エンチャント定義
     * @throws ReferencedDefinitionNotFoundException エンチャント定義が存在しない場合
     */
    private ItemEnchantmentDto requireEnchantmentDefinition(
            final String itemId,
            final String enchantmentId
    ) {
        final ItemEnchantmentDto enchantment =
                enchantmentRepository.findById(enchantmentId);

        if (enchantment == null) {
            throw new ReferencedDefinitionNotFoundException(
                    itemId,
                    "enchantments",
                    enchantmentId
            );
        }

        return enchantment;
    }

    /**
     * 属性定義を取得する。
     *
     * @param itemId      参照元アイテムID
     * @param attributeId 属性ID
     * @return 属性定義
     * @throws ReferencedDefinitionNotFoundException 属性定義が存在しない場合
     */
    private ItemAttributeDto requireAttributeDefinition(
            final String itemId,
            final String attributeId
    ) {
        final ItemAttributeDto attribute =
                attributeRepository.findById(attributeId);

        if (attribute == null) {
            throw new ReferencedDefinitionNotFoundException(
                    itemId,
                    "attributes",
                    attributeId
            );
        }

        return attribute;
    }

    /**
     * PotionEffect定義を取得する。
     *
     * @param itemId   参照元アイテムID
     * @param effectId 効果ID
     * @return 効果定義
     * @throws ReferencedDefinitionNotFoundException 効果定義が存在しない場合
     */
    private ItemEffectDto requireEffectDefinition(
            final String itemId,
            final String effectId
    ) {
        final ItemEffectDto effect =
                effectRepository.findById(effectId);

        if (effect == null) {
            throw new ReferencedDefinitionNotFoundException(
                    itemId,
                    "effects",
                    effectId
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
        return MessageUtil.mm(value);
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
}
