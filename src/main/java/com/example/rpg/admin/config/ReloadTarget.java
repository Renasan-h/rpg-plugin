package com.example.rpg.admin.config;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 設定ファイルの再読み込み対象を表す。
 */
public enum ReloadTarget {

    /**
     * すべての設定。
     */
    ALL("all"),

    /**
     * config.yml。
     */
    CONFIG("config"),

    /**
     * items.yml。
     */
    ITEMS("items"),

    /**
     * attributes.yml。
     */
    ATTRIBUTES("attributes"),

    /**
     * enchantments.yml。
     */
    ENCHANTMENTS("enchantments"),

    /**
     * effects.yml。
     */
    EFFECTS("effects"),

    /**
     * shop.yml。
     */
    SHOP("shop");

    /**
     * コマンド上の識別子。
     */
    private final String commandName;

    /**
     * 再読み込み対象を生成する。
     *
     * @param commandName コマンド上の識別子
     */
    ReloadTarget(final String commandName) {
        this.commandName = commandName;
    }

    /**
     * コマンド引数から再読み込み対象を取得する。
     *
     * @param value コマンド引数
     * @return 対応する再読み込み対象
     */
    public static Optional<ReloadTarget> fromCommandName(
            final String value
    ) {
        if (value == null) {
            return Optional.empty();
        }

        final String normalizedValue =
                value.toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(target ->
                        target.commandName.equals(normalizedValue))
                .findFirst();
    }

    /**
     * Tab補完用の識別子一覧を取得する。
     *
     * @return 変更不可能な識別子一覧
     */
    public static List<String> commandNames() {
        return Arrays.stream(values())
                .map(ReloadTarget::getCommandName)
                .toList();
    }

    /**
     * コマンド上の識別子を取得する。
     *
     * @return コマンド上の識別子
     */
    public String getCommandName() {
        return commandName;
    }
}
