package com.example.rpg.service;

import com.example.rpg.repository.interfaces.IMoneyRepository;
import org.bukkit.event.Listener;

/**
 * お金を管理するためのサービス
 * プレイヤーID（UUID)で紐づける
 * TODO: 将来的にDBか何かで管理する
 */
public class MoneyService implements Listener {

    /**
     * 所持金Repository
     */
    private final IMoneyRepository moneyRepository;

    /**
     * MoneyService生成
     *
     * @param moneyRepository 所持金管理Repository
     */
    public MoneyService(IMoneyRepository moneyRepository) {
        this.moneyRepository = moneyRepository;
    }
}
