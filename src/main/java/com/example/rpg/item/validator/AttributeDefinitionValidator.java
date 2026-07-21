package com.example.rpg.item.validator;

import com.example.rpg.common.exception.InvalidDefinitionException;
import com.example.rpg.item.dto.ItemAttributeDto;

import java.util.Collection;
import java.util.Objects;

/**
 * RPGアイテム用Attribute定義を検証するValidator。
 *
 * <p>
 * Attribute、Operation、EquipmentSlotGroupの存在確認は、
 * ItemAttributeDtoのコンストラクタで実施されている。
 * このValidatorでは、DTO単体では保証されていない値の妥当性を検証する。
 * </p>
 */
public final class AttributeDefinitionValidator {

    /**
     * Attribute定義を一括検証する。
     *
     * @param definitions 検証対象
     */
    public void validateAll(
            final Collection<ItemAttributeDto> definitions
    ) {
        Objects.requireNonNull(
                definitions,
                "definitions must not be null"
        );

        for (ItemAttributeDto definition : definitions) {
            validate(definition);
        }
    }

    /**
     * Attribute定義を検証する。
     *
     * @param definition 検証対象
     */
    public void validate(
            final ItemAttributeDto definition
    ) {
        Objects.requireNonNull(
                definition,
                "definition must not be null"
        );

        validateId(definition);
        validateAmount(definition);
    }

    /**
     * Attribute定義IDを検証する。
     *
     * @param definition 検証対象
     */
    private void validateId(
            final ItemAttributeDto definition
    ) {
        if (definition.getId().isBlank()) {
            throw new InvalidDefinitionException(
                    "attribute",
                    definition.getId(),
                    "id",
                    "Attribute definition ID must not be blank"
            );
        }
    }

    /**
     * Attributeの変更量を検証する。
     *
     * <p>
     * NaNや無限大をAttributeModifierへ渡すと、
     * 正常な能力値計算ができなくなるため禁止する。
     * </p>
     *
     * @param definition 検証対象
     */
    private void validateAmount(
            final ItemAttributeDto definition
    ) {
        final double amount = definition.getAmount();

        if (!Double.isFinite(amount)) {
            throw new InvalidDefinitionException(
                    "attribute",
                    definition.getId(),
                    "amount",
                    "Attribute amount must be a finite number"
                            + " / amount="
                            + amount
            );
        }
    }
}