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

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * RPG プラグイン独自の効果情報用Repository
 *
 * <p>
 * yaml structure</br>
 * effects:</br>
 * 　　effectId:</br>
 * 　　　　effectName:</br>
 * 　　　　type: string</br>
 * 　　　　duration: int</br>
 * 　　　　amplifier: int</br>
 * 　　　　ambient: boolean</br>
 * 　　　　particles: boolean</br>
 * 　　　　icon: boolean</br>
 * </p>
 */
public class YamlEffectRepository implements IEffectRepository {
    /**
     * 効果定義のルートセクション名
     */
    private static final String EFFECTS_SECTION_PATH = "effects";

    /**
     * 効果定義ファイル。
     */
    private final File configurationFile;

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
     * @param configurationFile effects.yml
     * @throws NullPointerException configurationFileがnullの場合
     */
    public YamlEffectRepository(
            final File configurationFile
    ) {
        this.configurationFile = Objects.requireNonNull(
                configurationFile,
                "configurationFile must not be null"
        );

        load();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * ディスク上のeffects.ymlを読み直し、全定義の解析成功後に
     * Repositoryのキャッシュを更新する。
     * </p>
     */
    @Override
    public void load() {
        final YamlConfiguration loadedConfiguration =
                YamlConfiguration.loadConfiguration(
                        configurationFile
                );

        final Map<String, ItemEffectDto> loadedEffects =
                loadEffects(loadedConfiguration);

        effects.clear();
        effects.putAll(loadedEffects);
    }

    /**
     * YAML設定からEffect定義一覧を読み込む。
     *
     * @param configuration effects.ymlの読込結果
     * @return Effect定義一覧
     */
    private Map<String, ItemEffectDto> loadEffects(
            final YamlConfiguration configuration
    ) {
        final ConfigurationSection effectsSection =
                configuration.getConfigurationSection(
                        EFFECTS_SECTION_PATH
                );

        if (effectsSection == null) {
            return Map.of();
        }

        final Map<String, ItemEffectDto> loadedEffects = new LinkedHashMap<>();

        for (String effectId : effectsSection.getKeys(false)) {
            final ConfigurationSection effectSection =
                    effectsSection.getConfigurationSection(effectId);

            if (effectSection == null) {
                throw new InvalidPropertyTypeException(
                        effectId,
                        "effects." + effectId,
                        "section",
                        effectSection.get(effectId)
                );
            }

            loadedEffects.put(
                    effectId,
                    loadEffect(effectId, effectSection)
            );
        }

        return loadedEffects;
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
        final String effectTypeName = requireString(
                section,
                "type",
                effectId,
                "effects." + effectId + ".type"
        );

        final PotionEffectType effectType =
                parsePotionEffectType(effectId, effectTypeName);

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

        final boolean ambient = getBooleanOrDefault(
                section,
                "ambient",
                effectId,
                "effects." + effectId + ".ambient",
                false
        );

        final boolean particles = getBooleanOrDefault(
                section,
                "particles",
                effectId,
                "effects." + effectId + ".particles",
                false
        );

        final boolean icon = getBooleanOrDefault(
                section,
                "icon",
                effectId,
                "effects." + effectId + ".icon",
                false
        );

        return new ItemEffectDto(
                effectId,
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
     * @param effectId       Effect定義ID
     * @param effectTypeName MinecraftのPotionEffect名
     * @return PotionEffectType
     */
    private PotionEffectType parsePotionEffectType(
            final String effectId,
            final String effectTypeName
    ) {
        final NamespacedKey effectKey =
                NamespacedKey.fromString(
                        effectTypeName.toLowerCase(Locale.ROOT)
                );

        if (effectKey == null) {
            throw new InvalidPropertyValueException(
                    effectId,
                    "effects." + effectId,
                    effectTypeName,
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
                    effectTypeName
            );
        }

        return effectType;
    }

    /**
     * 必須文字列値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         相対パス
     * @param effectId     Effect定義ID
     * @param propertyPath 完全なプロパティパス
     * @return 検証済み文字列
     */
    private String requireString(
            final ConfigurationSection section,
            final String path,
            final String effectId,
            final String propertyPath
    ) {
        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    effectId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof String value)) {
            throw new InvalidPropertyTypeException(
                    effectId,
                    propertyPath,
                    "string",
                    rawValue
            );
        }

        if (value.isBlank()) {
            throw new InvalidPropertyValueException(
                    effectId,
                    propertyPath,
                    rawValue,
                    "must not be blank"
            );
        }

        return value;
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
     * @param defaultValue デフォルト値
     * @return boolean値
     */
    private boolean getBooleanOrDefault(
            final ConfigurationSection section,
            final String path,
            final String effectId,
            final String propertyPath,
            final boolean defaultValue
    ) {
        if (!section.isSet(path)) {
            return defaultValue;
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
        return Map.copyOf(effects);
    }
}
