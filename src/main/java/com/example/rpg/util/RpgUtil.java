package com.example.rpg.util;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Rpg関連におけるUtilityをまとめるクラス
 */
public class RpgUtil {

    /**
     * 指定されたセクションから指定したキーをもとに値を取得し、返却する
     *
     * @param section 設定ファイルのセクション情報
     * @param path    セクションに存在するキー
     * @param def     デフォルト値
     * @return {@link int} 取得した値
     */
    public static int getIntOrDefault(ConfigurationSection section, String path, int def) {
        int value;
        if (section.isInt(path)) {
            value = section.getInt(path);
        } else {
            try {
                value = Integer.parseInt(section.getString(path));
            } catch (NumberFormatException e) {
                value = def;
            }
        }

        return value;
    }

    /**
     * 指定された文字列を数値へ変換します。
     * 変換時に例外が発生した場合は-1を返します。
     *
     * @param val 変換対象文字列
     * @return value 返還後数値（失敗時は-1）
     */
    public static int getIntOrDefault(String val) {
        int value;
        try {
            value = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            value = -1;
        }
        return value;
    }
}
