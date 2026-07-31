package com.example.rpg.bank.repository;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 * プレイヤーの銀行残高をYAMLファイルで管理するRepositoryです。
 *
 * <p>
 * 銀行残高をメモリ章へ保存し、必要に応じで
 * {@code bank.yml}と読み書きを行います。
 * </p>
 *
 * <p>
 * 入金・出金などの業務ルールは担当せず、
 * 最終的な銀行残高の取得と保存だけを担当します。
 * </p>
 */
public class YamlBankRepository implements IBankRepository {

    /**
     * 銀行YAMLファイル名
     */
    private static final String FILE_NAME = "bank.yml";

    /**
     * YAML銀行残高セクションパス
     */
    private static final String BALANCE_PATH = "balances";

    /**
     * プラグインインスタンス
     */
    private final JavaPlugin plugin;

    /**
     * 銀行残高ファイル
     */
    private final File bankFile;

    /**
     * 銀行残高用Map
     */
    private final Map<UUID, Integer> balanceMap;

    /**
     * YAML形式の銀行残高Repositoryを生成します。
     *
     * @param plugin プラグイン本体
     * @throws NullPointerException pluginがnullの場合
     */
    public YamlBankRepository(final JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin must not be null"
        );
        this.bankFile = new File(
                plugin.getDataFolder(),
                FILE_NAME
        );
        this.balanceMap = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findBalance(
            final UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        return balanceMap.getOrDefault(
                playerId,
                0
        );
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException YAMLファイルの保存に失敗した場合
     */
    @Override
    public int setBalance(
            final UUID playerId,
            final int amount
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative: " + amount
            );
        }

        balanceMap.put(
                playerId,
                amount
        );

        save();

        return amount;
    }

    @Override
    public void setBalances(
            final Map<UUID, Integer> balances
    ) {
        Objects.requireNonNull(
                balances,
                "balances must not be null"
        );

        for (final Map.Entry<UUID, Integer> entry : balances.entrySet()) {
            final UUID playerId = Objects.requireNonNull(
                    entry.getKey(),
                    "playerId must not be null"
            );

            final int balance = entry.getValue();

            if (balance < 0) {
                throw new IllegalArgumentException(
                        "amount must not be negative: " + balance
                );
            }

            balanceMap.put(playerId, balance);
        }
        save();
    }

    /**
     * {@code bank.yml}から銀行残高を読み込みます。
     *
     * <p>
     * ファイルが存在しない場合は空のファイルを作成し、
     * 銀行残高が存在しない状態として扱います。
     * </p>
     */
    public void load() {
        ensureDataFolderExists();

        if (!bankFile.exists()) {
            save();
        }

        final ConfigurationSection configuration =
                YamlConfiguration.loadConfiguration(bankFile);

        final ConfigurationSection balancesSection =
                configuration.getConfigurationSection(BALANCE_PATH);

        balanceMap.clear();

        if (balancesSection == null) {
            return;
        }

        for (final String playerIdText : balancesSection.getKeys(false)) {
            loadBalance(
                    balancesSection,
                    playerIdText
            );
        }
    }

    /**
     * メモリ上の銀行残高を{@code bank.yml}へ保存します。
     *
     * @throws IllegalStateException YAMLファイルの保存に失敗した場合
     */
    public void save() {
        ensureDataFolderExists();

        final YamlConfiguration configuration =
                new YamlConfiguration();

        for (final Map.Entry<UUID, Integer> entry : balanceMap.entrySet()) {
            final String path = BALANCE_PATH + "." + entry.getKey();

            configuration.set(path, entry.getValue());
        }

        try {
            configuration.save(bankFile);
        } catch (final IOException ex) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "bank.ymlの保存に失敗しました。",
                    ex
            );

            throw new IllegalStateException(
                    "Failed to save bank data.",
                    ex
            );
        }
    }

    /**
     * YAMLセクションからプレイヤー1人分の銀行残高を読み込みます。
     *
     * <p>
     * UUIDまたは残高が不正なデータは読み飛ばし、
     * 警告ログを出力します。
     * </p>
     *
     * @param balancesSection 銀行残高セクション
     * @param playerIdText    UUID文字列
     */
    private void loadBalance(
            final ConfigurationSection balancesSection,
            final String playerIdText
    ) {
        try {
            final UUID playerId = UUID.fromString(playerIdText);

            final int balance = balancesSection.getInt(playerIdText);

            if (balance < 0) {
                plugin.getLogger().warning(
                        "負数の銀行残高を読み飛ばしました。"
                                + " plyerId="
                                + playerId
                                + ", 銀行残高="
                                + balance
                );
                return;
            }

            balanceMap.put(
                    playerId,
                    balance
            );
        } catch (final IllegalArgumentException ex) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "不正なUUIDの銀行残高データを読み飛ばしました。"
                            + " playerId="
                            + playerIdText,
                    ex
            );
        }
    }

    /**
     * プラグインのデータフォルダが存在することを保証します。
     */
    private void ensureDataFolderExists() {
        final File dataFolder =
                plugin.getDataFolder();

        if (dataFolder.exists()) {
            return;
        }

        if (!dataFolder.mkdirs()) {
            throw new IllegalStateException(
                    "Failed to create plugin data folder: "
                            + dataFolder.getAbsolutePath()
            );
        }
    }
}
