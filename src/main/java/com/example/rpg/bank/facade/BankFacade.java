package com.example.rpg.bank.facade;

import com.example.rpg.bank.service.BankService;

import java.util.Objects;
import java.util.UUID;

/**
 * 銀行機能のユースケースを外部層へ提供するFacadeです。
 *
 * <p>
 * Commandなどのプレゼンテーション層は、
 * BankServiceを直接操作せず、このFacadeを経由します。
 * </p>
 */
public class BankFacade {

    /**
     * 銀行業務Service
     */
    private final BankService bankService;

    /**
     * BankFacadeを生成します。
     *
     * @param bankService 銀行業務Service
     * @throws NullPointerException bankServiceがnullの場合
     */
    public BankFacade(final BankService bankService) {
        this.bankService = Objects.requireNonNull(
                bankService,
                "bankService must not be null"
        );
    }

    /**
     * プレイヤーの銀行残高を取得する。
     *
     * @param playerId プレイヤーUUID
     * @return 銀行残高
     */
    public int getBalance(final UUID playerId) {
        return bankService.getBalance(playerId);
    }

    /**
     * プレイヤーの手持ちから銀行へ入金する。
     *
     * @param playerId プレイヤーUUID
     * @param amount   入金金額
     * @return 入金後の銀行残高
     */
    public int deposit(
            final UUID playerId,
            final int amount
    ) {
        return bankService.deposit(
                playerId,
                amount
        );
    }

    /**
     * 銀行から手持ちへ出金する。
     *
     * @param playerId プレイヤーUUID
     * @param amount   出勤金額
     * @return 出金後銀行残高
     */
    public int withdraw(
            final UUID playerId,
            final int amount
    ) {
        return bankService.withdraw(
                playerId,
                amount
        );
    }

    /**
     * プレイヤーの銀行残高から送金対象プレイヤーの銀行口座へ送金する。
     *
     * @param sernderId  送金元プレイヤーUUID
     * @param receiverId 送金先プレイヤーUUID
     * @param amount     送金金額
     */
    public void transfer(
            final UUID sernderId,
            final UUID receiverId,
            final int amount
    ) {
        bankService.transfer(
                sernderId,
                receiverId,
                amount
        );
    }
}
