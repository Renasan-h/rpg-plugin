package com.example.rpg.common.exception;

/**
 * 設定ファイルの読込または検証に失敗した場合の基底例外です。
 *
 * <p>設定値に関する例外は、このクラスを継承して原因ごとに分類します。</p>
 */
public class ConfigurationException extends RuntimeException {

    /**
     * 指定されたメッセージで設定例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public ConfigurationException(final String message) {
        super(message);
    }

    /**
     * 指定されたメッセージと原因例外で設定例外を生成します。
     *
     * @param message 例外メッセージ
     * @param cause   原因となった例外
     */
    public ConfigurationException(
            final String message,
            final Throwable cause
    ) {
        super(message, cause);
    }
}