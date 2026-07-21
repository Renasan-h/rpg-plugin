package com.example.rpg.item.validator;

import com.example.rpg.common.exception.InvalidDefinitionException;
import com.example.rpg.item.dto.ItemEffectDto;

import java.util.Map;
import java.util.Objects;

/**
 * RPGアイテム用PotionEffect定義を検証するValidator。
 */
public final class EffectDefinitionValidator {

    /**
     * Effect定義を一括検証する。
     *
     * @param definitions 検証対象
     */
    public void validateAll(
            final Map<String, ItemEffectDto> definitions
    ) {
        Objects.requireNonNull(
                definitions,
                "definitions must not be null"
        );

        for (Map.Entry<String, ItemEffectDto> entry
                : definitions.entrySet()) {

            validate(
                    entry.getKey(),
                    entry.getValue()
            );
        }
    }

    /**
     * Effect定義を検証する。
     *
     * @param definitionId Repository上の定義ID
     * @param definition   検証対象
     */
    public void validate(
            final String definitionId,
            final ItemEffectDto definition
    ) {
        Objects.requireNonNull(
                definitionId,
                "definitionId must not be null"
        );
        Objects.requireNonNull(
                definition,
                "definition must not be null"
        );

        validateId(definitionId, definition);
        validateDuration(definitionId, definition);
        validateAmplifier(definitionId, definition);
    }

    /**
     * Effect定義IDを検証する。
     *
     * <p>
     * RepositoryのMapキーとDTO内部のeffectIdが異なる場合、
     * 参照時の挙動が分かりにくくなるため不正とする。
     * </p>
     *
     * @param definitionId Repository上の定義ID
     * @param definition   検証対象
     */
    private void validateId(
            final String definitionId,
            final ItemEffectDto definition
    ) {
        if (definitionId.isBlank()) {
            throw new InvalidDefinitionException(
                    "effect",
                    definitionId,
                    "id",
                    "Effect definition ID must not be blank"
            );
        }

        if (!definitionId.equals(definition.getEffectId())) {
            throw new InvalidDefinitionException(
                    "effect",
                    definitionId,
                    "effectId",
                    "Repository ID and DTO effect ID must match"
                            + " / dtoEffectId="
                            + definition.getEffectId()
            );
        }
    }

    /**
     * 効果時間を検証する。
     *
     * @param definitionId 定義ID
     * @param definition   検証対象
     */
    private void validateDuration(
            final String definitionId,
            final ItemEffectDto definition
    ) {
        if (definition.getDuration() < 1) {
            throw new InvalidDefinitionException(
                    "effect",
                    definitionId,
                    "duration",
                    "Effect duration must be 1 tick or greater"
                            + " / duration="
                            + definition.getDuration()
            );
        }
    }

    /**
     * 効果レベルを検証する。
     *
     * <p>
     * Bukkitのamplifierは0がレベル1を表すため、
     * 負数は許可しない。
     * </p>
     *
     * @param definitionId 定義ID
     * @param definition   検証対象
     */
    private void validateAmplifier(
            final String definitionId,
            final ItemEffectDto definition
    ) {
        if (definition.getAmplifier() < 0) {
            throw new InvalidDefinitionException(
                    "effect",
                    definitionId,
                    "amplifier",
                    "Effect amplifier must be 0 or greater"
                            + " / amplifier="
                            + definition.getAmplifier()
            );
        }
    }
}