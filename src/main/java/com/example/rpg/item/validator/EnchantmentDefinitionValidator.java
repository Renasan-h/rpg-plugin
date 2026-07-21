package com.example.rpg.item.validator;

import com.example.rpg.common.exception.InvalidDefinitionException;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;
import java.util.Objects;

/**
 * RPGアイテム用Enchantment定義を検証するValidator。
 */
public final class EnchantmentDefinitionValidator {

    /**
     * Enchantment定義を一括検証する。
     *
     * <p>
     * Enchantment DTOには独自IDが含まれていないため、
     * Repositoryが保持するMapのキーを定義IDとして使用する。
     * </p>
     *
     * @param definitions 検証対象
     */
    public void validateAll(
            final Map<String, ItemEnchantmentDto> definitions
    ) {
        Objects.requireNonNull(
                definitions,
                "definitions must not be null"
        );

        for (Map.Entry<String, ItemEnchantmentDto> entry
                : definitions.entrySet()) {

            validate(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }

    /**
     * Enchantment定義を検証する。
     *
     * @param definitionId 定義ID
     * @param definition   検証対象
     */
    public void validate(
            final String definitionId,
            final ItemEnchantmentDto definition
    ) {
        Objects.requireNonNull(
                definitionId,
                "definitionId must not be null"
        );
        Objects.requireNonNull(
                definition,
                "definition must not be null"
        );

        validateId(definitionId);
        validateLevel(definitionId, definition);
    }

    /**
     * 定義IDを検証する。
     *
     * @param definitionId 定義ID
     */
    private void validateId(final String definitionId) {
        if (definitionId.isBlank()) {
            throw new InvalidDefinitionException(
                    "enchantment",
                    definitionId,
                    "id",
                    "Enchantment definition ID must not be blank"
            );
        }
    }

    /**
     * Enchantmentレベルを検証する。
     *
     * <p>
     * ignoreLevelRestrictionがtrueの場合でも、
     * 0以下のレベルはアイテム定義として認めない。
     * </p>
     *
     * @param definitionId 定義ID
     * @param definition   検証対象
     */
    private void validateLevel(
            final String definitionId,
            final ItemEnchantmentDto definition
    ) {
        final int level = definition.getLevel();
        final Enchantment enchantment =
                definition.getEnchantment();

        if (level < 1) {
            throw new InvalidDefinitionException(
                    "enchantment",
                    definitionId,
                    "level",
                    "Enchantment level must be 1 or greater"
                            + " / level="
                            + level
            );
        }

        /*
         * 標準レベル制限を無視する設定の場合は、
         * Bukkitが定義する最大レベルを超えることを許可する。
         */
        if (definition.isIgnoreLevelRestriction()) {
            return;
        }

        final int minimumLevel =
                enchantment.getStartLevel();
        final int maximumLevel =
                enchantment.getMaxLevel();

        if (level < minimumLevel || level > maximumLevel) {
            throw new InvalidDefinitionException(
                    "enchantment",
                    definitionId,
                    "level",
                    "Enchantment level is outside the allowed range"
                            + " / enchantment="
                            + enchantment.getKey()
                            + " / level="
                            + level
                            + " / allowedRange="
                            + minimumLevel
                            + "-"
                            + maximumLevel
            );
        }
    }
}