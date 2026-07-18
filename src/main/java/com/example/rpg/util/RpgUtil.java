package com.example.rpg.util;

/**
 * Rpg関連におけるUtilityをまとめるクラス
 */
public class RpgUtil {

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
