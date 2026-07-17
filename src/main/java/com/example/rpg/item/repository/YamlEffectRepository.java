package com.example.rpg.item.repository;

import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.RequiredPropertyException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.item.dto.ItemEffectDto;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * RPG プラグイン独自の効果情報用Repository
 */
public class YamlEffectRepository implements IEffectRepository {
    /**
     * 効果定義のルートセクション名
     */
    private static final String EFFECTS_SECTION_PATH = "effects";

    /**
     * effects.ymlの読み込み結果
     */
    private final YamlConfiguration config;

    /**
     * 読み込み済み効果定義
     *
     * <p>
     * キーは効果ID、値は効果定義とする。
     * MapはRepository内部でのみ使用する。
     * </p>
     */
    private final Map<String, ItemEffectDto> effects =
            new LinkedHashMap<>();

    /**
     * YAML形式のItemEffectRepositoryを生成する。
     *
     * @param config items.ymlの読み込み結果
     * @throws NullPointerException configがnullの場合
     */
    public YamlEffectRepository(final YamlConfiguration config) {
        this.config = Objects.requireNonNull(
                config,
                "config must not be null"
        );

        load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        final ConfigurationSection effectsSection =
                config.getConfigurationSection(EFFECTS_SECTION_PATH);

        if (effectsSection == null) {
            return;
        }

        // reload時に削除済みの定義が残らないよう、
        // 新しい定義を読み込む前にキャッシュを破棄する
        effects.clear();

        for (String key : effectsSection.getKeys(false)) {
            final ConfigurationSection effectSection =
                    effectsSection.getConfigurationSection(key);

            if (effectSection == null) {
                continue;
            }

            effects.put(key, loadEffect(key, effectSection));
        }
    }

    /**
     * Effectを読み込む。
     *
     * @param effectId 効果ID
     * @param section  Effect設定
     * @return Effect情報
     */
    private ItemEffectDto loadEffect(
            final String effectId,
            final ConfigurationSection section
    ) {
        final PotionEffectType effectType =
                parsePotionEffectType(effectId);
        final int duration = requireInt(
                section,
                "duration",
                effectId,
                "effects." + effectId + ".duration",
                1
        );

        final int amplifier = requireInt(
                section,
                "amplifier",
                effectId,
                "effects." + effectId + ".amplifier",
                0
        );

        final boolean ambient = requireBoolean(
                section,
                "ambient",
                effectId,
                "effects." + effectId + ".ambient"
        );

        final boolean particles = requireBoolean(
                section,
                "particles",
                effectId,
                "effects." + effectId + ".particles"
        );

        final boolean icon = requireBoolean(
                section,
                "icon",
                effectId,
                "effects." + effectId + ".icon"
        );

        return new ItemEffectDto(
                effectType,
                duration,
                amplifier,
                ambient,
                particles,
                icon
        );
    }

    /**
     * PotionEffect名をPotionEffectTypeへ変換する。
     *
     * @param effectId 効果ID
     * @return PotionEffectType
     */
    private PotionEffectType parsePotionEffectType(
            final String effectId
    ) {
        final NamespacedKey effectKey =
                NamespacedKey.fromString(
                        effectId.toLowerCase(Locale.ROOT)
                );

        if (effectKey == null) {
            throw new InvalidPropertyValueException(
                    effectId,
                    "effects." + effectId,
                    "must be a valid namespaced key"
            );
        }

        final Registry<PotionEffectType> effectRegistry =
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.MOB_EFFECT);

        final PotionEffectType effectType =
                effectRegistry.get(effectKey);

        if (effectType == null) {
            throw new UnknownConfigurationValueException(
                    effectId,
                    "effects." + effectId,
                    effectId
            );
        }

        return effectType;
    }

    /**
     * 必須整数値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         相対パス
     * @param effectId     効果ID
     * @param propertyPath 完全なプロパティパス
     * @param minimum      最小値
     * @return 検証済み整数値
     */
    private int requireInt(
            final ConfigurationSection section,
            final String path,
            final String effectId,
            final String propertyPath,
            final int minimum
    ) {
        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    effectId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    effectId,
                    propertyPath,
                    "integer",
                    rawValue
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)
                || value != Math.rint(value)) {
            throw new InvalidPropertyValueException(
                    effectId,
                    propertyPath,
                    rawValue,
                    "must be a finite integer"
            );
        }

        if (value < minimum
                || value > Integer.MAX_VALUE) {
            throw new InvalidPropertyValueException(
                    effectId,
                    propertyPath,
                    rawValue,
                    "must be between"
                            + minimum
                            + " and "
                            + Integer.MAX_VALUE
            );
        }

        return (int) value;
    }

    /**
     * 必須boolean値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         相対パス
     * @param effectId     効果ID
     * @param propertyPath 完全なプロパティパス
     * @return boolean値
     */
    private boolean requireBoolean(
            final ConfigurationSection section,
            final String path,
            final String effectId,
            final String propertyPath
    ) {
        if (!section.isSet(path)) {
            return false;
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof Boolean value)) {
            throw new InvalidPropertyTypeException(
                    effectId,
                    propertyPath,
                    "boolean",
                    rawValue
            );
        }

        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemEffectDto findById(final String effectId) {
        return effects.get(effectId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ItemEffectDto> findAll() {
        return effects;
    }
}
