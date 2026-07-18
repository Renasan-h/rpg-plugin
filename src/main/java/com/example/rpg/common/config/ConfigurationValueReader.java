package com.example.rpg.common.config;

import com.example.rpg.common.exception.InvalidPropertyTypeException;
import com.example.rpg.common.exception.InvalidPropertyValueException;
import com.example.rpg.common.exception.RequiredPropertyException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * YAML設定値の取得と基本検証を行うReader
 *
 * <p>
 * 必須判定、型判定、数値範囲判定等
 * Repositoryインスタンス生成時に使用する共通機能
 * </p>
 */
public final class ConfigurationValueReader {

    /**
     * インスタンス生成を禁止する。
     */
    private ConfigurationValueReader() {
    }

    /**
     * 必須文字列値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @return 検証済み文字列
     */
    public static String requireString(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    definitionId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof String value)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "string",
                    rawValue
            );
        }

        if (value.isBlank()) {
            throw new InvalidPropertyValueException(
                    definitionId,
                    propertyPath,
                    rawValue,
                    "must not be blank"
            );
        }

        return value;
    }

    /**
     * デフォルト値付き文字列を取得する。
     *
     * <p>
     * 設定が存在する場合は型と空文字を検証する。
     * </p>
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param defaultValue デフォルト値
     * @return 検証済み文字列
     */
    public static String getStringOrDefault(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath,
            final String defaultValue
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        Objects.requireNonNull(
                defaultValue,
                "defaultValue must not be null"
        );

        if (!section.isSet(path)) {
            return defaultValue;
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof String value)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "string",
                    rawValue
            );
        }

        if (value.isBlank()) {
            throw new InvalidPropertyValueException(
                    definitionId,
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
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param minimum      最小値
     * @param maximum      最大値
     * @return 検証済み整数値
     */
    public static int requireInt(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath,
            final int minimum,
            final int maximum
    ) {
        validateRange(minimum, maximum);

        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    definitionId,
                    propertyPath
            );
        }

        return readInt(
                section.get(path),
                definitionId,
                propertyPath,
                minimum,
                maximum
        );
    }

    /**
     * デフォルト値付き整数値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param defaultValue デフォルト値
     * @param minimum      最小値
     * @param maximum      最大値
     * @return 検証済み整数値
     */
    public static int getIntOrDefault(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath,
            final int defaultValue,
            final int minimum,
            final int maximum
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );
        validateRange(minimum, maximum);

        if (defaultValue < minimum
                || defaultValue > maximum) {
            throw new IllegalArgumentException(
                    "defaultValue must be between "
                            + minimum
                            + " and "
                            + maximum
            );
        }

        if (!section.isSet(path)) {
            return defaultValue;
        }

        return readInt(
                section.get(path),
                definitionId,
                propertyPath,
                minimum,
                maximum
        );
    }

    /**
     * 必須の有限なdouble値を取得する。
     *
     * <p>
     * YAML上の整数値と小数値の両方を受け入れるが、
     * NaNおよび正負の無限大は拒否する。
     * </p>
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @return 検証済みdouble値
     */
    public static double requireDouble(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    definitionId,
                    propertyPath
            );
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "number",
                    rawValue
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)) {
            throw new InvalidPropertyValueException(
                    definitionId,
                    propertyPath,
                    rawValue,
                    "must be a finite number"
            );
        }

        return value;
    }

    /**
     * デフォルト値付きboolean値を取得する。
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param defaultValue デフォルト値
     * @return boolean値
     */
    public static boolean getBooleanOrDefault(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath,
            final boolean defaultValue
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        if (!section.isSet(path)) {
            return defaultValue;
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof Boolean value)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "boolean",
                    rawValue
            );
        }

        return value;
    }

    /**
     * 必須の子セクションを取得する。
     *
     * @param section      親セクション
     * @param path         子セクションの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @return 子セクション
     */
    public static ConfigurationSection requireSection(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        if (!section.isSet(path)) {
            throw new RequiredPropertyException(
                    definitionId,
                    propertyPath
            );
        }

        final ConfigurationSection childSection =
                section.getConfigurationSection(path);

        if (childSection == null) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "section",
                    section.get(path)
            );
        }

        return childSection;
    }

    /**
     * 任意の文字列リストを取得する。
     *
     * <p>
     * 未指定の場合は空の変更不可能なリストを返す。
     * リスト以外の値、文字列以外の要素、空文字の要素は拒否する。
     * </p>
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @return 検証済み文字列リスト
     */
    public static List<String> getStringListOrEmpty(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );

        if (!section.isSet(path)) {
            return List.of();
        }

        final Object rawValue = section.get(path);

        if (!(rawValue instanceof List<?> rawList)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "list",
                    rawValue
            );
        }

        final List<String> values =
                new ArrayList<>(rawList.size());

        for (int index = 0;
             index < rawList.size();
             index++) {

            final Object rawElement =
                    rawList.get(index);

            if (!(rawElement instanceof String value)) {
                throw new InvalidPropertyTypeException(
                        definitionId,
                        propertyPath + "[" + index + "]",
                        "string",
                        rawElement
                );
            }

            if (value.isBlank()) {
                throw new InvalidPropertyValueException(
                        definitionId,
                        propertyPath + "[" + index + "]",
                        value,
                        "must not be blank"
                );
            }

            values.add(value);
        }

        return List.copyOf(values);
    }

    /**
     * 任意の整数値を取得する。
     *
     * <p>
     * プロパティが未指定の場合はnullを返す。
     * 設定されている場合は、整数型と指定範囲を検証する。
     * </p>
     *
     * @param section      読込対象セクション
     * @param path         セクションからの相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param minimum      最小値
     * @param maximum      最大値
     * @return 検証済み整数値。未指定の場合はnull
     */
    public static Integer getOptionalInt(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath,
            final int minimum,
            final int maximum
    ) {
        validateArguments(
                section,
                path,
                definitionId,
                propertyPath
        );
        validateRange(minimum, maximum);

        if (!section.isSet(path)) {
            return null;
        }

        return readInt(
                section.get(path),
                definitionId,
                propertyPath,
                minimum,
                maximum
        );
    }

    /**
     * Object値を整数として検証する。
     *
     * @param rawValue     元の設定値
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     * @param minimum      最小値
     * @param maximum      最大値
     * @return 検証済み整数値
     */
    private static int readInt(
            final Object rawValue,
            final String definitionId,
            final String propertyPath,
            final int minimum,
            final int maximum
    ) {
        if (!(rawValue instanceof Number number)) {
            throw new InvalidPropertyTypeException(
                    definitionId,
                    propertyPath,
                    "integer",
                    rawValue
            );
        }

        final double value = number.doubleValue();

        if (!Double.isFinite(value)
                || value != Math.rint(value)) {
            throw new InvalidPropertyValueException(
                    definitionId,
                    propertyPath,
                    rawValue,
                    "must be a finite integer"
            );
        }

        if (value < minimum
                || value > maximum) {
            throw new InvalidPropertyValueException(
                    definitionId,
                    propertyPath,
                    rawValue,
                    "must be between "
                            + minimum
                            + " and "
                            + maximum
            );
        }

        return (int) value;
    }

    /**
     * 共通引数を検証する。
     *
     * @param section      セクション
     * @param path         相対パス
     * @param definitionId 定義ID
     * @param propertyPath 完全なプロパティパス
     */
    private static void validateArguments(
            final ConfigurationSection section,
            final String path,
            final String definitionId,
            final String propertyPath
    ) {
        Objects.requireNonNull(
                section,
                "section must not be null"
        );
        Objects.requireNonNull(
                path,
                "path must not be null"
        );
        Objects.requireNonNull(
                definitionId,
                "definitionId must not be null"
        );
        Objects.requireNonNull(
                propertyPath,
                "propertyPath must not be null"
        );
    }

    /**
     * 数値範囲を検証する。
     *
     * @param minimum 最小値
     * @param maximum 最大値
     */
    private static void validateRange(
            final int minimum,
            final int maximum
    ) {
        if (minimum > maximum) {
            throw new IllegalArgumentException(
                    "minimum must not be greater than maximum"
            );
        }
    }
}
