package com.example.rpg.item.builder;

import com.example.rpg.item.pdc.ItemPdcKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * ItemStackを構築するBuilder。
 *
 * <p>
 * Repository、DTO、YAMLには依存せず、Factoryによって解決された
 * Bukkit APIの値を使用してItemStackを構築する。
 * </p>
 *
 * <p>
 * BuilderはItemStack生成ごとに新しく作成し、再利用しない。
 * </p>
 */
public final class ItemBuilder {

    /**
     * RPGアイテムで使用するPDCキー。
     */
    private final ItemPdcKeys itemPdcKeys;

    /**
     * アイテムへ適用するエンチャント一覧。
     */
    private final List<EnchantmentEntry> enchantments =
            new ArrayList<>();

    /**
     * アイテムへ適用する属性一覧。
     */
    private final List<AttributeEntry> attributes =
            new ArrayList<>();

    /**
     * アイテム（ポーション）へ適用する効果一覧。
     */
    private final List<PotionEffect> effects =
            new ArrayList<>();

    /**
     * ItemStackのMaterial。
     */
    private Material material;

    /**
     * ItemStackの個数。
     */
    private int amount = 1;

    /**
     * アイテムの表示名。
     */
    private Component displayName;

    /**
     * アイテムのLore。
     */
    private List<Component> lore = List.of();

    /**
     * アイテムへ設定するItemFlag。
     */
    private Collection<ItemFlag> itemFlags = List.of();

    /**
     * 耐久値を減少させない場合true。
     */
    private boolean unbreakable;

    /**
     * CustomModelData。
     *
     * <p>
     * 未指定の場合はnull。
     * </p>
     */
    private Integer customModelData;

    /**
     * PDCへ保存するRPGアイテムID。
     */
    private String itemId;

    /**
     * ItemBuilderを生成する。
     *
     * @param itemPdcKeys RPGアイテム用PDCキー
     * @throws NullPointerException itemPdcKeysがnullの場合
     */
    private ItemBuilder(final ItemPdcKeys itemPdcKeys) {
        this.itemPdcKeys = Objects.requireNonNull(
                itemPdcKeys,
                "itemPdcKeys must not be null"
        );
    }

    /**
     * 新しいItemBuilderを生成する。
     *
     * @param itemPdcKeys RPGアイテム用PDCキー
     * @return 新しいItemBuilder
     */
    public static ItemBuilder builder(final ItemPdcKeys itemPdcKeys) {
        return new ItemBuilder(itemPdcKeys);
    }

    /**
     * Materialを設定する。
     *
     * @param material Material
     * @return this builder
     */
    public ItemBuilder material(final Material material) {
        this.material = Objects.requireNonNull(
                material,
                "material must not be null"
        );
        return this;
    }

    /**
     * アイテム個数を設定する。
     *
     * @param amount アイテム個数
     * @return this builder
     */
    public ItemBuilder amount(final int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * 表示名を設定する。
     *
     * @param displayName 表示名
     * @return this builder
     */
    public ItemBuilder displayName(final Component displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Loreを設定する。
     *
     * @param lore Lore
     * @return this builder
     * @throws NullPointerException loreがnullの場合
     */
    public ItemBuilder lore(final List<Component> lore) {
        this.lore = List.copyOf(
                Objects.requireNonNull(
                        lore,
                        "lore must not be null"
                )
        );
        return this;
    }

    /**
     * ItemFlagを設定する。
     *
     * @param itemFlags ItemFlag一覧
     * @return this builder
     * @throws NullPointerException itemFlagsがnullの場合
     */
    public ItemBuilder itemFlags(
            final Collection<ItemFlag> itemFlags
    ) {
        this.itemFlags = List.copyOf(
                Objects.requireNonNull(
                        itemFlags,
                        "itemFlags must not be null"
                )
        );
        return this;
    }

    /**
     * 耐久値を減少させないか設定する。
     *
     * @param unbreakable 耐久値を減少させない場合true
     * @return this builder
     */
    public ItemBuilder unbreakable(final boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * CustomModelDataを設定する。
     *
     * @param customModelData CustomModelData。未指定の場合はnull
     * @return this builder
     */
    public ItemBuilder customModelData(
            final Integer customModelData
    ) {
        this.customModelData = customModelData;
        return this;
    }

    /**
     * RPGアイテムIDを設定する。
     *
     * @param itemId RPGアイテムID
     * @return this builder
     */
    public ItemBuilder itemId(final String itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * エンチャントを追加する。
     *
     * @param enchantment            エンチャント
     * @param level                  エンチャントレベル
     * @param ignoreLevelRestriction 標準のレベル制限を無視する場合true
     * @return this builder
     * @throws NullPointerException enchantmentがnullの場合
     */
    public ItemBuilder enchantment(
            final Enchantment enchantment,
            final int level,
            final boolean ignoreLevelRestriction
    ) {
        enchantments.add(
                new EnchantmentEntry(
                        Objects.requireNonNull(
                                enchantment,
                                "enchantment must not be null"
                        ),
                        level,
                        ignoreLevelRestriction
                )
        );

        return this;
    }

    /**
     * AttributeModifierを追加する。
     *
     * @param attribute 対象Attribute
     * @param modifier  AttributeModifier
     * @return this builder
     * @throws NullPointerException 引数がnullの場合
     */
    public ItemBuilder attribute(
            final Attribute attribute,
            final AttributeModifier modifier
    ) {
        attributes.add(
                new AttributeEntry(
                        Objects.requireNonNull(
                                attribute,
                                "attribute must not be null"
                        ),
                        Objects.requireNonNull(
                                modifier,
                                "modifier must not be null"
                        )
                )
        );

        return this;
    }

    /**
     * PotionEffectを追加する。
     *
     * @param effect PotionEffect
     * @return this Builder
     * @throws NullPointerException effectがnullの場合
     */
    public ItemBuilder effect(
            final PotionEffect effect
    ) {
        effects.add(
                Objects.requireNonNull(
                        effect,
                        "effect must not be null"
                )
        );

        return this;
    }

    /**
     * ItemStackを生成する。
     *
     * @return 生成したItemStack
     * @throws IllegalStateException    Materialが設定されていない場合
     * @throws IllegalArgumentException 個数が不正な場合
     */
    public ItemStack build() {
        validate();

        final ItemStack itemStack =
                new ItemStack(material, amount);

        final ItemMeta meta = itemStack.getItemMeta();

        applyDisplayName(meta);
        applyLore(meta);
        applyItemFlags(meta);
        applyUnbreakable(meta);
        applyCustomModelData(meta);
        applyEnchantments(meta);
        applyAttributes(meta);
        applyEffects(meta);
        applyPersistentData(meta);

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    /**
     * Builderへ設定された値を検証する。
     */
    private void validate() {
        if (material == null) {
            throw new IllegalStateException(
                    "material must be specified before build"
            );
        }

        if (amount < 1 || amount > material.getMaxStackSize()) {
            throw new IllegalArgumentException(
                    "amount is out of range: "
                            + "amount="
                            + amount
                            + ", maxStackSize="
                            + material.getMaxStackSize()
            );
        }
    }

    /**
     * 表示名をItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyDisplayName(final ItemMeta meta) {
        if (displayName == null) {
            return;
        }

        meta.displayName(displayName);
    }

    /**
     * LoreをItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyLore(final ItemMeta meta) {
        if (lore.isEmpty()) {
            return;
        }

        meta.lore(new ArrayList<>(lore));
    }

    /**
     * ItemFlagをItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyItemFlags(final ItemMeta meta) {
        if (itemFlags.isEmpty()) {
            return;
        }

        meta.addItemFlags(
                itemFlags.toArray(ItemFlag[]::new)
        );
    }

    /**
     * Unbreakable設定をItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyUnbreakable(final ItemMeta meta) {
        meta.setUnbreakable(unbreakable);
    }

    /**
     * CustomModelDataをItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    @SuppressWarnings("deprecation")
    private void applyCustomModelData(final ItemMeta meta) {
        if (customModelData == null) {
            return;
        }

        meta.setCustomModelData(customModelData);
    }

    /**
     * エンチャントをItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyEnchantments(final ItemMeta meta) {
        for (EnchantmentEntry entry : enchantments) {
            meta.addEnchant(
                    entry.enchantment(),
                    entry.level(),
                    entry.ignoreLevelRestriction()
            );
        }
    }

    /**
     * 属性をItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyAttributes(final ItemMeta meta) {
        for (AttributeEntry entry : attributes) {
            meta.addAttributeModifier(
                    entry.attribute,
                    entry.attributeModifier
            );
        }
    }

    /**
     * 効果をItemMetaへ適用する。
     *
     * @param meta ItemMeta
     */
    private void applyEffects(final ItemMeta meta) {
        for (EnchantmentEntry entry : enchantments) {
            meta.addEnchant(
                    entry.enchantment(),
                    entry.level(),
                    entry.ignoreLevelRestriction()
            );
        }
    }

    /**
     * RPGアイテムIDをPDCへ保存する。
     *
     * @param meta ItemMeta
     */
    private void applyPersistentData(final ItemMeta meta) {
        if (itemId == null || itemId.isBlank()) {
            return;
        }

        meta.getPersistentDataContainer().set(
                itemPdcKeys.getItemIdKey(),
                PersistentDataType.STRING,
                itemId
        );
    }

    /**
     * Builder内部で使用するエンチャント設定。
     *
     * <p>
     * DTOではなく、ItemMetaへそのまま適用できる値だけを保持する。
     * </p>
     *
     * @param enchantment            エンチャント
     * @param level                  エンチャントレベル
     * @param ignoreLevelRestriction レベル制限を無視する場合true
     */
    private record EnchantmentEntry(
            Enchantment enchantment,
            int level,
            boolean ignoreLevelRestriction
    ) {
    }

    /**
     * Builder内部で使用する属性設定。
     *
     * <p>
     * DTOではなく、ItemMetaへそのまま適用できる値だけを保持する。
     * </p>
     *
     * @param attribute         属性
     * @param attributeModifier {@link AttributeModifier}
     */
    private record AttributeEntry(
            Attribute attribute,
            AttributeModifier attributeModifier
    ) {
    }
}