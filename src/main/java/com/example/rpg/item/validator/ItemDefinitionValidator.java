package com.example.rpg.item.validator;

import com.example.rpg.common.exception.InvalidDefinitionException;
import com.example.rpg.item.dto.ItemDto;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.repository.interfaces.IAttributeRepository;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.*;

/**
 * RPGアイテム定義の整合性を検証するValidator。
 *
 * <p>
 * ItemDto単体の検証に加えて、Attribute、Enchantment、Effectの
 * 参照先が存在するかを検証する。
 * </p>
 */
public final class ItemDefinitionValidator {

    /**
     * Attribute定義Repository。
     */
    private final IAttributeRepository attributeRepository;

    /**
     * Enchantment定義Repository。
     */
    private final IEnchantmentRepository enchantmentRepository;

    /**
     * Effect定義Repository。
     */
    private final IEffectRepository effectRepository;

    /**
     * Validatorを生成する。
     *
     * @param attributeRepository   Attribute定義Repository
     * @param enchantmentRepository Enchantment定義Repository
     * @param effectRepository      Effect定義Repository
     */
    public ItemDefinitionValidator(
            final IAttributeRepository attributeRepository,
            final IEnchantmentRepository enchantmentRepository,
            final IEffectRepository effectRepository
    ) {
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
    }

    /**
     * アイテム定義を一括検証する。
     *
     * @param definitions 検証対象
     */
    public void validateAll(
            final Collection<ItemDto> definitions
    ) {
        Objects.requireNonNull(
                definitions,
                "definitions must not be null"
        );

        for (ItemDto definition : definitions) {
            validate(definition);
        }
    }

    /**
     * アイテム定義を検証する。
     *
     * @param definition 検証対象
     */
    public void validate(final ItemDto definition) {
        Objects.requireNonNull(
                definition,
                "definition must not be null"
        );

        validateId(definition);
        validateMaterial(definition);
        validateDuplicateReferences(definition);
        validateAttributeReferences(definition);
        validateEnchantmentReferences(definition);
        validateEffectReferences(definition);
        validatePotionMeta(definition);
        validateEnchantmentCompatibility(definition);
    }

    /**
     * アイテムIDを検証する。
     *
     * @param definition 検証対象
     */
    private void validateId(final ItemDto definition) {
        if (definition.getId().isBlank()) {
            throw invalid(
                    definition,
                    "id",
                    "Item definition ID must not be blank"
            );
        }
    }

    /**
     * Materialを検証する。
     *
     * @param definition 検証対象
     */
    private void validateMaterial(
            final ItemDto definition
    ) {
        final Material material =
                definition.getMaterial();

        if (material == Material.AIR) {
            throw invalid(
                    definition,
                    "material",
                    "AIR cannot be used as an RPG item material"
            );
        }

        /*
         * ブロック専用など、ItemStackとして使用できないMaterialを拒否する。
         */
        if (!material.isItem()) {
            throw invalid(
                    definition,
                    "material",
                    "Material cannot be represented as an item"
                            + " / material="
                            + material.name()
            );
        }
    }

    /**
     * 各参照IDの重複を検証する。
     *
     * @param definition 検証対象
     */
    private void validateDuplicateReferences(
            final ItemDto definition
    ) {
        validateNoDuplicates(
                definition,
                "attributes",
                definition.getAttributes()
        );
        validateNoDuplicates(
                definition,
                "enchantments",
                definition.getEnchantments()
        );
        validateNoDuplicates(
                definition,
                "effects",
                definition.getEffects()
        );
    }

    /**
     * 一覧内に同じ参照IDが複数存在しないことを検証する。
     *
     * @param definition   アイテム定義
     * @param propertyName プロパティ名
     * @param references   参照ID一覧
     */
    private void validateNoDuplicates(
            final ItemDto definition,
            final String propertyName,
            final List<String> references
    ) {
        final Set<String> uniqueReferences =
                new HashSet<>();

        for (String referenceId : references) {
            if (!uniqueReferences.add(referenceId)) {
                throw invalid(
                        definition,
                        propertyName,
                        "Duplicate referenced definition ID"
                                + " / referencedId="
                                + referenceId
                );
            }
        }
    }

    /**
     * Attribute参照先の存在を検証する。
     *
     * @param definition 検証対象
     */
    private void validateAttributeReferences(
            final ItemDto definition
    ) {
        for (String attributeId
                : definition.getAttributes()) {

            if (attributeRepository.findById(attributeId) == null) {
                throw invalid(
                        definition,
                        "attributes",
                        "Referenced attribute definition was not found"
                                + " / referencedId="
                                + attributeId
                );
            }
        }
    }

    /**
     * Enchantment参照先の存在を検証する。
     *
     * @param definition 検証対象
     */
    private void validateEnchantmentReferences(
            final ItemDto definition
    ) {
        for (String enchantmentId
                : definition.getEnchantments()) {

            if (enchantmentRepository.findById(enchantmentId) == null) {
                throw invalid(
                        definition,
                        "enchantments",
                        "Referenced enchantment definition was not found"
                                + " / referencedId="
                                + enchantmentId
                );
            }
        }
    }

    /**
     * Effect参照先の存在を検証する。
     *
     * @param definition 検証対象
     */
    private void validateEffectReferences(
            final ItemDto definition
    ) {
        for (String effectId : definition.getEffects()) {
            if (effectRepository.findById(effectId) == null) {
                throw invalid(
                        definition,
                        "effects",
                        "Referenced effect definition was not found"
                                + " / referencedId="
                                + effectId
                );
            }
        }
    }

    /**
     * Effect設定とPotionMetaの整合性を検証する。
     *
     * @param definition 検証対象
     */
    private void validatePotionMeta(
            final ItemDto definition
    ) {
        if (definition.getEffects().isEmpty()) {
            return;
        }

        final ItemStack itemStack =
                new ItemStack(definition.getMaterial());

        final ItemMeta itemMeta =
                itemStack.getItemMeta();

        if (!(itemMeta instanceof PotionMeta)) {
            throw invalid(
                    definition,
                    "effects",
                    "Potion effects can only be configured "
                            + "for materials that use PotionMeta"
                            + " / material="
                            + definition.getMaterial().name()
            );
        }
    }

    /**
     * EnchantmentとMaterialの互換性を検証する。
     *
     * <p>
     * レベル制限を無視する設定であっても、
     * Materialとの互換性までは無視しない。
     * </p>
     *
     * @param definition 検証対象
     */
    private void validateEnchantmentCompatibility(
            final ItemDto definition
    ) {
        if (definition.getEnchantments().isEmpty()) {
            return;
        }

        final ItemStack itemStack =
                new ItemStack(definition.getMaterial());

        for (String enchantmentId
                : definition.getEnchantments()) {

            final ItemEnchantmentDto enchantmentDefinition =
                    enchantmentRepository.findById(
                            enchantmentId
                    );

            /*
             * 参照先の存在は前段で検証済みだが、
             * 単独メソッドとして呼ばれた場合の安全性も考慮する。
             */
            if (enchantmentDefinition == null) {
                continue;
            }

            if (!enchantmentDefinition
                    .getEnchantment()
                    .canEnchantItem(itemStack)) {

                throw invalid(
                        definition,
                        "enchantments",
                        "Enchantment cannot be applied to material"
                                + " / referencedId="
                                + enchantmentId
                                + " / enchantment="
                                + enchantmentDefinition
                                .getEnchantment()
                                .getKey()
                                + " / material="
                                + definition.getMaterial().name()
                );
            }
        }
    }

    /**
     * アイテム定義不正例外を生成する。
     *
     * @param definition   アイテム定義
     * @param propertyName プロパティ名
     * @param reason       不正理由
     * @return 定義不正例外
     */
    private InvalidDefinitionException invalid(
            final ItemDto definition,
            final String propertyName,
            final String reason
    ) {
        return new InvalidDefinitionException(
                "item",
                definition.getId(),
                propertyName,
                reason
        );
    }
}