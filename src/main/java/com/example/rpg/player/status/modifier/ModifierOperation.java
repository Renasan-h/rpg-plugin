package com.example.rpg.player.status.modifier;

/**
 * ステータス補正の演算方法を表します。
 */
public enum ModifierOperation {

    /**
     * 固定値を加算します。
     *
     * <p>
     * 例: ATTACKへ20を加算する場合、基礎値100は120になります。
     * </p>
     */
    ADD,

    /**
     * 基準値に対して割合補正を適用します。
     *
     * <p>
     * 値は10,000を100.00%として扱います。
     * 例えば2,000は20.00%の加算補正です。
     * </p>
     */
    MULTIPLY
}