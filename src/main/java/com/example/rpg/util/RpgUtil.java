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

    public static int getIntOrDefault(String val, int def) {
        int value;
        try {
            value = Integer.parseInt(val);
        } catch (NumberFormatException e) {
            value = 0;
        }
        return value;
    }
}
