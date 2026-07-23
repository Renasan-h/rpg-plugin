package com.example.rpg.item.repository;

import com.example.rpg.common.config.ConfigurationValueReader;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.UnknownConfigurationValueException;
import com.example.rpg.common.repository.AbstractYamlRepository;
import com.example.rpg.item.dto.ItemEnchantmentDto;
import com.example.rpg.item.repository.interfaces.IEnchantmentRepository;
import com.example.rpg.item.validator.EnchantmentDefinitionValidator;
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
 *
 * <p>
 * yaml structure</br>
 * enchantments:</br>
 * 　　enchantmentId:</br>
 * 　　　　level: {@link Integer}</br>
 * 　　　　ignoreLevelRestriction: {@link Boolean}</br>
 * </p>
 */
public class YamlEnchantmentRepository extends AbstractYamlRepository<Map<String, ItemEnchantmentDto>> implements IEnchantmentRepository {
    /**
     * Enchantment定義のルートセクション名
     */
    private static final String ENCHANTMENTS_SECTION_PATH = "enchantments";

    /**
     * Enchantment定義Validator
     */
    private final EnchantmentDefinitionValidator validator;

    /**
     * YAML形式のEnchantmentRepositoryを生成する。
     *
     * @param configurationFile enchantments.yml
     * @param validator         Enchantment定義Validator
     * @throws NullPointerException 引数がnullの場合
     */
    public YamlEnchantmentRepository(
            final File configurationFile,
            final EnchantmentDefinitionValidator validator
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

        /*
         * sharpnessとminecraft:sharpnessのように、
         * 正規化後に同じNamespacedKeyとなる定義を検知する。
         */
        final Set<NamespacedKey> loadedKey = new HashSet<NamespacedKey>();

        for (String enchantmentId : enchantmentsSection.getKeys(false)) {
            final String propertyRoot =
                    ENCHANTMENTS_SECTION_PATH
                            + "."
                            + enchantmentId;

            final ConfigurationSection enchantmentSection =
                    ConfigurationValueReader.requireSection(
                            enchantmentsSection,
                            enchantmentId,
                            enchantmentId,
                            propertyRoot
                    );

            loadedEnchantments.put(
                    enchantmentId,
                    loadEnchantment(
                            enchantmentId,
                            propertyRoot,
                            enchantmentSection,
                            loadedKey,
                            enchantmentRegistry
                    )
            );
        }

        return loadedEnchantments;
    }

    /**
     * 読み込んだEnchantment定義を検証する。
     *
     * @param candidateData 検証対象のEnchantment定義
     */
    @Override
    protected void validate(final Map<String, ItemEnchantmentDto> candidateData) {
        validator.validateAll(candidateData);
    }

    /**
     * Enchantmentを読み込む。
     *
     * @param enchantmentId       効果ID
     * @param propertyRoot        定義の完全パス
     * @param section             Enchantment設定
     * @param loadedKeys          エンチャント重複確認用
     * @param enchantmentRegistry minecraftに登録されているエンチャントの定義
     * @return Enchantment情報
     */
    private ItemEnchantmentDto loadEnchantment(
            final String enchantmentId,
            final String propertyRoot,
            final ConfigurationSection section,
            final Set<NamespacedKey> loadedKeys,
            final Registry<Enchantment> enchantmentRegistry
    ) {
        final NamespacedKey enchantmentKey =
                parseEnchantmentKey(
                        enchantmentId,
                        propertyRoot
                );

        validateDuplicateKey(
                enchantmentId,
                propertyRoot,
                enchantmentKey,
                loadedKeys
        );

        final Enchantment enchantment =
                findEnchantment(
                        enchantmentId,
                        propertyRoot,
                        enchantmentKey,
                        enchantmentRegistry
                );

        final int level = ConfigurationValueReader.requireInt(
                section,
                "level",
                enchantmentId,
                propertyRoot + ".level",
                1,
                Integer.MAX_VALUE
        );

        final boolean ignoreLevelRestriction =
                ConfigurationValueReader.getBooleanOrDefault(
                        section,
                        "ignoreLevelRestriction",
                        enchantmentId,
                        propertyRoot
                                + ".ignoreLevelRestriction",
                        false
                );

        validateEnchantmentLevel(
                enchantmentId,
                propertyRoot,
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
     * Enchantment定義IDをNamespacedKeyへ変換する。
     *
     * <p>
     * 名前空間が省略された場合はminecraft名前空間を使用する。
     * </p>
     *
     * @param enchantmentId Enchantment定義ID
     * @param propertyRoot  定義の完全パス
     * @return NamespacedKey
     */
    private NamespacedKey parseEnchantmentKey(
            final String enchantmentId,
            final String propertyRoot
    ) {
        final String normalizedName =
                enchantmentId.toLowerCase(Locale.ROOT);

        final NamespacedKey key =
                NamespacedKey.fromString(normalizedName);

        if (key == null) {
            throw new InvalidPropertyValueException(
                    enchantmentId,
                    propertyRoot,
                    enchantmentId,
                    "enchantment key"
            );
        }

        return key;
    }

    /**
     * 正規化後のEnchantmentキーが重複していないことを確認する。
     *
     * @param enchantmentId  Enchantment定義ID
     * @param propertyRoot   定義の完全パス
     * @param enchantmentKey Enchantmentキー
     * @param loadedKeys     読込済みキー
     */
    private void validateDuplicateKey(
            final String enchantmentId,
            final String propertyRoot,
            final NamespacedKey enchantmentKey,
            final Set<NamespacedKey> loadedKeys
    ) {
        if (loadedKeys.add(enchantmentKey)) {
            return;
        }

        throw new InvalidPropertyValueException(
                enchantmentId,
                propertyRoot,
                enchantmentKey.toString(),
                "duplicates another enchantment key"
        );
    }

    /**
     * Bukkit RegistryからEnchantmentを取得する。
     *
     * @param enchantmentId       Enchantment定義ID
     * @param propertyRoot        定義の完全パス
     * @param enchantmentKey      Enchantmentキー
     * @param enchantmentRegistry Bukkit Registry
     * @return Enchantment
     */
    private Enchantment findEnchantment(
            final String enchantmentId,
            final String propertyRoot,
            final NamespacedKey enchantmentKey,
            final Registry<Enchantment> enchantmentRegistry
    ) {
        final Enchantment enchantment =
                enchantmentRegistry.get(enchantmentKey);

        if (enchantment == null) {
            throw new UnknownConfigurationValueException(
                    enchantmentId,
                    propertyRoot,
                    enchantmentKey.toString()
            );
        }

        return enchantment;
    }

    /**
     * エンチャントレベルを検証する。
     *
     * @param enchantmentId          エンチャントID
     * @param propertyRoot           定義の完全パス
     * @param enchantment            エンチャント
     * @param level                  設定レベル
     * @param ignoreLevelRestriction レベル制限を無視する場合true
     */
    private void validateEnchantmentLevel(
            final String enchantmentId,
            final String propertyRoot,
            final Enchantment enchantment,
            final int level,
            final boolean ignoreLevelRestriction
    ) {
        if (ignoreLevelRestriction) {
            return;
        }

        final int minimumLevel = enchantment.getStartLevel();
        final int maximumLevel = enchantment.getMaxLevel();

        if (level >= minimumLevel
                && level <= maximumLevel) {
            return;
        }

        throw new InvalidPropertyValueException(
                enchantmentId,
                propertyRoot + ".level",
                level,
                "must be between "
                        + minimumLevel
                        + "and "
                        + maximumLevel
        );
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
