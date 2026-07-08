package com.example.rpg.repository.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤー所持金を管理するRepository。
 *
 * <p>money.ymlの読み書きと、UUID単位の所持金取得・更新を担当する。
 * ServiceがYAMLやMap構造へ依存しないよう、永続化の詳細をこのRepositoryに閉じ込める。</p>
 */
public interface IMoneyRepository {
    /**
     * メモリ上に保持するプレイヤー所持金。
     *
     * <p>頻繁に参照される値のため、毎回YAMLを読むのではなくメモリに保持する。</p>
     */
    Map<UUID, Integer> moneyMap = new HashMap<>();

    /**
     * プレイヤーの所持金を取得する。
     *
     * @param playerId 対象プレイヤーUUID
     * @return 所持金。未登録の場合は0
     */
    int findMoney(UUID playerId);

    /**
     * プレイヤーの所持金を加算する。
     *
     * @param playerId 対象プレイヤーUUID
     * @param amount   加算額
     * @return 加算後の所持金
     */
    int addMoney(UUID playerId, int amount);

    /**
     * プレイヤーの所持金を設定する。
     *
     * @param playerId 対象プレイヤーUUID
     * @param amount   設定額
     * @return 設定後の所持金
     */
    int setMoney(UUID playerId, int amount);

    /**
     * 所持金が足りる場合のみ減算する。
     *
     * <p>購入処理では「確認してから減算」だと途中で状態が変わる可能性があるため、
     * Repository側で不足チェックと更新を一つの操作として扱う。</p>
     *
     * @param playerId 対象プレイヤーUUID
     * @param amount   減算額
     * @return 減算できた場合true
     */
    boolean subtractMoney(UUID playerId, int amount);

    /**
     * money.ymlを読み込む。
     */
    void load();

    /**
     * 所持金情報をmoney.ymlへ保存する。
     */
    void save();
}