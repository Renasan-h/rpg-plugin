package com.example.rpg.item.repository;

import com.example.rpg.common.exception.ConfigurationException;
import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.util.*;

/**
 * RPG プラグイン独自のエンチャント情報用Repository
 */
public class YamlEnchantmentRepository extends AbstractYamlRepository<Map<String, ItemEnchantmentDto>> implements IEnchantmentRepository {
    /**
     * 効果定義のルートセクション名
     */
    private static final String ENCHANTMENTS_SECTION_PATH = "enchantments";

    /**
     * YAML形式のItemEnchantmentRepositoryを生成する。
     *
     * @param configurationFile enchantments.ymlの読み込み結果
     */
    public YamlEnchantmentRepository(final File configurationFile) {
        super(configurationFile);

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
     * YAML設定からEnchantment定義一覧を読み込む。
     *
     * @param configuration enchantments.ymlの読込結果
     * @return Enchantment定義一覧
     */
    @Override
    protected Map<String, ItemEnchantmentDto> parse(
            final YamlConfiguration configuration
    ) {
        final ConfigurationSection enchantmentsSection =
                configuration.getConfigurationSection(
                        ENCHANTMENTS_SECTION_PATH
                );

        if (enchantmentsSection == null) {
            return Map.of();
        }

        final Map<String, ItemEnchantmentDto> loadedEnchantments = new LinkedHashMap<>();

        final Registry<Enchantment> enchantmentRegistry =
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.ENCHANTMENT);

        Set<NamespacedKey> loadedKey = new HashSet<NamespacedKey>();

        for (String enchantmentId : enchantmentsSection.getKeys(false)) {
            final ConfigurationSection enchantmentSection =
                    enchantmentsSection.getConfigurationSection(
                            enchantmentId
                    );

            if (enchantmentSection == null) {
                throw new ConfigurationException(
                        "Enchantment定義がセクションではありません。"
                                + " / enchantmentId="
                                + enchantmentId
                );
            }

            loadedEnchantments.put(
                    enchantmentId,
                    loadEnchantment(
                            enchantmentId,
                            enchantmentSection,
                            loadedKey,
                            enchantmentRegistry
                    )
            );
        }

        return loadedEnchantments;
    }

    /**
     * Enchantmentを読み込む。
     *
     * @param enchantmentId       効果ID
     * @param section             Enchantment設定
     * @param loadedKeys          エンチャント重複確認用
     * @param enchantmentRegistry minecraftに登録されているエンチャントの定義
     * @return Enchantment情報
     */
    private ItemEnchantmentDto loadEnchantment(
            final String enchantmentId,
            final ConfigurationSection section,
            final Set<NamespacedKey> loadedKeys,
            final Registry<Enchantment> enchantmentRegistry
    ) {
        final NamespacedKey enchantmentKey =
                parseEnchantmentKey(
                        enchantmentId
                );

        if (!loadedKeys.add(enchantmentKey)) {
            throw new InvalidPropertyTypeException(
                    enchantmentId,
                    "enchantment",
                    "Duplicate",
                    enchantmentKey
            );
        }

        final Enchantment enchantment =
                enchantmentRegistry.get(enchantmentKey);

        if (enchantment == null) {
            throw new UnknownConfigurationValueException(
                    enchantmentId,
                    "enchantment",
                    enchantmentKey.getKey()
            );
        }

        final int level = loadEnchantmentLevel(
                enchantmentId,
                enchantmentKey,
                section
        );

        final boolean ignoreLevelRestriction =
                section.getBoolean(
                        "ignoreLevelRestriction",
                        false
                );

        validateEnchantmentLevel(
                enchantmentId,
                enchantment,
                level,
                ignoreLevelRestriction
        );

        return new ItemEnchantmentDto(
                enchantment,
                level,
                ignoreLevelRestriction
        );
    }

    /**
     * エンチャント名をNamespacedKeyへ変換する。
     *
     * <p>
     * 名前空間が省略された場合はminecraft名前空間を使用する。
     * </p>
     *
     * @param enchantmentId エンチャントID
     * @return NamespacedKey
     */
    private NamespacedKey parseEnchantmentKey(
            final String enchantmentId
    ) {
        final String normalizedName =
                enchantmentId.toLowerCase(Locale.ROOT);

        final NamespacedKey key =
                NamespacedKey.fromString(normalizedName);

        if (key == null) {
            throw new InvalidPropertyValueException(
                    enchantmentId,
                    ENCHANTMENTS_SECTION_PATH + "." + enchantmentId,
                    "enchantment key"
            );
        }

        return key;
    }

    /**
     * エンチャントレベルを読み込む。
     *
     * @param enchantmentId      エンチャントID
     * @param enchantmentKey     エンチャントキー
     * @param enchantmentSection エンチャント設定
     * @return エンチャントレベル
     */
    private int loadEnchantmentLevel(
            final String enchantmentId,
            final NamespacedKey enchantmentKey,
            final ConfigurationSection enchantmentSection
    ) {
        final Object rawLevel =
                enchantmentSection.get("level");

        if (!(rawLevel instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    enchantmentId,
                    "Enchantment",
                    enchantmentKey.getKey(),
                    "level must be an integer"
            );
        }

        final double doubleValue = number.doubleValue();

        if (!Double.isFinite(doubleValue)
                || doubleValue != Math.rint(doubleValue)
                || doubleValue < 1
                || doubleValue > Integer.MAX_VALUE
        ) {
            throw new InvalidPropertyValueException(
                    enchantmentId,
                    "enchantments." + enchantmentKey + ".level",
                    rawLevel,
                    "must be greater than or equal to 1"
            );
        }

        return (int) doubleValue;
    }

    /**
     * エンチャントレベルを検証する。
     *
     * @param enchantmentId          エンチャントID
     * @param enchantment            エンチャント
     * @param level                  設定レベル
     * @param ignoreLevelRestriction レベル制限を無視する場合true
     */
    private void validateEnchantmentLevel(
            final String enchantmentId,
            final Enchantment enchantment,
            final int level,
            final boolean ignoreLevelRestriction
    ) {
        if (ignoreLevelRestriction) {
            return;
        }

        if (level < enchantment.getStartLevel()
                || level > enchantment.getMaxLevel()) {
            throw new InvalidPropertyValueException(
                    enchantmentId,
                    "enchantments." + enchantment.getKey() + ".level",
                    level,
                    "must be between 1 and " + enchantment.getMaxLevel()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemEnchantmentDto findById(final String enchantmentId) {
        return getCurrentData().get(enchantmentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, ItemEnchantmentDto> findAll() {
        return getCurrentData();
    }
}
