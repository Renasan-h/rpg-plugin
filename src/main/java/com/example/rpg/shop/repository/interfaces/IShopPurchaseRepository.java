package com.example.rpg.shop.repository.interfaces;

import java.util.UUID;

public interface IShopPurchaseRepository {
    /**
     * 指定商品の購入回数を取得する。
     *
     * @param playerId プレイヤーUUID
     * @param itemId   商品ID
     * @return 購入回数
     */
    int findPurchaseCount(UUID playerId, String itemId);

    /**
     * 指定商品の購入回数を1加算する。
     *
     * @param playerId プレイヤーUUID
     * @param itemId   商品ID
     */
    void incrementPurchaseCount(UUID playerId, String itemId);

    /**
     * 指定プレイヤーの購入履歴を削除する。
     *
     * @param playerId プレイヤーUUID
     */
    void resetPlayer(UUID playerId);

    /**
     * 指定プレイヤーの指定商品購入履歴を削除する。
     *
     * @param playerId プレイヤーUUID
     * @param itemId   商品ID
     */
    void resetItem(UUID playerId, String itemId);

    /**
     * 全購入履歴を削除する。
     */
    void resetAll();

    /**
     * shop-purchases.ymlを読み込む。
     */
    void load();

    /**
     * 購入履歴をshop-purchases.ymlへ保存する。
     */
    void save();
}
