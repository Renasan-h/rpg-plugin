package com.example.rpg.item.repository;

import com.example.rpg.common.config.ConfigurationValueReader;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.item.dto.ItemEffectDto;
import com.example.rpg.item.repository.interfaces.IEffectRepository;
import com.example.rpg.item.validator.EffectDefinitionValidator;
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
 * 　　　　type: {@link String}</br>
 * 　　　　duration: {@link Integer}</br>
 * 　　　　amplifier: {@link Integer}</br>
 * 　　　　ambient: {@link Boolean}</br>
 * 　　　　particles: {@link Boolean}</br>
 * 　　　　icon: {@link Boolean}</br>
 * </p>
 */
public class YamlEffectRepository extends AbstractYamlRepository<Map<String, ItemEffectDto>> implements IEffectRepository {
    /**
     * 効果定義のルートセクション名
     */
    private static final String EFFECTS_SECTION_PATH = "effects";

    /**
     * Effect定義Validator。
     */
    private final EffectDefinitionValidator validator;

    /**
     * YAML形式のItemEffectRepositoryを生成する。
     *
     * @param configurationFile effects.yml
     * @param validator         Effect定義validator
     * @throws NullPointerException 引数がnullの場合
     */
    public YamlEffectRepository(
            final File configurationFile,
            final EffectDefinitionValidator validator
    ) {
        super(configurationFile);

        this.validator = Objects.requireNonNull(
                validator,
                "validator must not be null"
        );
        load();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load() {
        reloadData();
    }

    /**
     * YAML設定からEffect定義一覧を読み込む。
     *
     * @param configuration effects.ymlの読込結果
     * @return Effect定義一覧
     */
    @Override
    protected Map<String, ItemEffectDto> parse(
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
                    ConfigurationValueReader.requireSection(
                            effectsSection,
                            effectId,
                            effectId,
                            "effects." + effectId
                    );

            loadedEffects.put(
                    effectId,
                    loadEffect(effectId, effectSection)
            );
        }

        return loadedEffects;
    }

    /**
     * 読み込んだEffect定義を検証する。
     *
     * @param candidateData 検証対象のEffect定義
     */
    @Override
    protected void validate(
            final Map<String, ItemEffectDto> candidateData
    ) {
        validator.validateAll(candidateData);
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
        final String propertyRoot =
                "effects." + effectId;

        final String effectTypeName = ConfigurationValueReader.requireString(
                section,
                "type",
                effectId,
                propertyRoot + ".type"
        );

        final PotionEffectType effectType =
                parsePotionEffectType(effectId, effectTypeName);

        final int duration = ConfigurationValueReader.requireInt(
                section,
                "duration",
                effectId,
                propertyRoot + ".duration",
                1,
                Integer.MAX_VALUE
        );

        final int amplifier = ConfigurationValueReader.requireInt(
                section,
                "amplifier",
                effectId,
                propertyRoot + ".amplifier",
                0,
                Integer.MAX_VALUE
        );

        final boolean ambient = ConfigurationValueReader.getBooleanOrDefault(
                section,
                "ambient",
                effectId,
                propertyRoot + ".ambient",
                false
        );

        final boolean particles = ConfigurationValueReader.getBooleanOrDefault(
                section,
                "particles",
                effectId,
                propertyRoot + ".particles",
                false
        );

        final boolean icon = ConfigurationValueReader.getBooleanOrDefault(
                section,
                "icon",
                effectId,
                propertyRoot + ".icon",
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
     * {@inheritDoc}
     */
    @Override
    public ItemEffectDto findById(final String effectId) {
        return getCurrentData().get(effectId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ItemEffectDto> findAll() {
        return Map.copyOf(getCurrentData());
    }
}
