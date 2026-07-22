package com.example.rpg.common.repository;

/**
 * 再読み込み可能なRepositoryを表す共通インターフェース。
 *
 * <p>
 * YAMLなどの外部データソースから定義を読み込み、
 * Repository内部の状態を更新するRepositoryが実装する。
 * </p>
 *
 * <p>
 * このインターフェースは読み込み操作のみを定義する。
 * 定義内容の検証責務はValidatorへ分離する。
 * </p>
 */
public interface ReloadableRepository {

    /**
     * 外部データソースかwら定義を読み込む・
     *
     * <p>
     * 読込に失敗した場合は、呼び出しもとへ例外を返却する。
     * </p>
     */
    void load();
}
