package com.example.rpg.common.repository;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Objects;

/**
 * YAMLファイルを使用するRepositoryの共通基底クラス。
 *
 * <p>
 * ディスクからのYAML読込、候補データの検証、
 * 検証成功後のデータ差し替えを担当する。
 * </p>
 *
 * <p>
 * YAML構造の解析、DTOへの変換および具体的な検証処理は、
 * 継承先RepositoryとValidatorが担当する。
 * </p>
 *
 * @param <T> Repositoryが保持する読込済みデータ型
 */
public abstract class AbstractYamlRepository<T> {

    /**
     * 読込対象のYAMLファイル
     */
    private final File configurationFile;

    /**
     * 正常に読み込まれ、検証された現在のデータ
     */
    private T currentData;

    /**
     * YAML Repositoryを生成する。
     *
     * <p>
     * このコンストラクタでは、オーバーライドされたメソッドを
     * 呼び出さない。継承先の初期化が完了する前にparseを実行すると、
     * 未初期化フィールドを参照する危険があるためである。
     * </p>
     *
     * @param configurationFile 読込対象ファイル
     * @throws NullPointerException configurationFileがnullの場合
     */
    protected AbstractYamlRepository(
            final File configurationFile
    ) {
        this.configurationFile = Objects.requireNonNull(
                configurationFile,
                "configurationFile must not be null"
        );
    }

    /**
     * ディスク上のYAMLファイルを再読み込みする。
     * 検証完了後にデータを置き換える。
     *
     * <p>
     * {@link #parse(YamlConfiguration)}が正常終了した場合のみ、
     * 現在のデータを差し替える。
     * </p>
     */
    protected final void reloadData() {
        final YamlConfiguration configuration =
                YamlConfiguration.loadConfiguration(configurationFile);

        final T candidateData = Objects.requireNonNull(
                parse(configuration),
                "parse result must not be null"
        );

        validate(candidateData);

        this.currentData = candidateData;
    }

    /**
     * YAML設定をRepository固有のデータへ変換する。
     *
     * @param configuration YAML読込結果
     * @return 検証済みデータ
     */
    protected abstract T parse(
            YamlConfiguration configuration
    );

    /**
     * 読み込んだ候補データを検証する。
     *
     * <p>
     * 具体的な検証は、継承先RepositoryからValidatorへ委譲する。
     * </p>
     *
     * @param candidateData 検証対象の候補データ
     */
    protected abstract void validate(T candidateData);

    /**
     * 正常に読み込まれた現在のデータを取得する。
     *
     * @return 現在のデータ
     * @throws IllegalStateException 初回読込前の場合
     */
    protected final T getCurrentData() {
        if (currentData == null) {
            throw new IllegalArgumentException(
                    "Repository data has not loaded"
            );
        }

        return currentData;
    }

    /**
     * 読込対象ファイル名を取得する。
     *
     * <p>
     * ログや例外メッセージへ利用する。
     * </p>
     *
     * @return ファイル名
     */
    protected final String getConfigurationFileName() {
        return configurationFile.getName();
    }

}
