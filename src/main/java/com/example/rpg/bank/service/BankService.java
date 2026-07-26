package com.example.rpg.bank.service;

import com.example.rpg.bank.repository.IBankRepository;
import com.example.rpg.event.publisher.BusinessEventPublisher;
import com.example.rpg.money.event.MoneyChangeReason;
import com.example.rpg.money.service.MoneyService;

import java.util.Objects;
import java.util.UUID;

/**
 * 銀行サービスです。
 *
 * <p>
 * 銀行残高に関する業務処理を提供します。
 * Repositoryには永続化のみを委譲し、
 * 入金・出金・送金などの業務ルールは本クラスで管理します。
 * </p>
 */
public class BankService {

    /**
     * 銀行残高Repositoryです。
     */
    private final IBankRepository bankRepository;

    /**
     * 所持金サービスです。
     */
    private final MoneyService moneyService;

    /**
     * 業務イベントPublisherです。
     */
    private final BusinessEventPublisher eventPublisher;

    /**
     * BankServiceを生成します。
     *
     * @param bankRepository Repository
     * @param moneyService   所持金サービス
     * @param eventPublisher Event Publisher
     */
    public BankService(
            final IBankRepository bankRepository,
            final MoneyService moneyService,
            final BusinessEventPublisher eventPublisher
    ) {

        this.bankRepository = Objects.requireNonNull(
                bankRepository,
                "bankRepository must not be null"
        );

        this.moneyService = Objects.requireNonNull(
                moneyService,
                "moneyService must not be null"
        );

        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "eventPublisher must not be null"
        );
    }

    /**
     * プレイヤーの銀行残高を取得します。
     *
     * @param playerId プレイヤーUUID
     * @return 銀行残高
     */
    public int getBalance(final UUID playerId) {
        validatePlayerId(playerId);

        return bankRepository.findBalance(playerId);
    }

    /**
     * 手持ち所持金から銀行へ入金します。
     *
     * <p>
     * 手持ち所持金の減算処理はMoneyServiceへ委譲します。
     * 所持金が不足している場合は、MoneyServiceから
     * InsufficientMoneyExceptionが送出されます。
     * </p>
     *
     * @param playerId 入金するプレイヤーのUUID
     * @param amount   入金額
     * @return 入金後の銀行残高
     * @throws NullPointerException     playerIdがnullの場合
     * @throws IllegalArgumentException amountが0以下の場合
     * @throws ArithmeticException      銀行残高の加算時にオーバーフローした場合
     */
    public int deposit(
            final UUID playerId,
            final int amount
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        validatePositiveAmount(amount);

        moneyService.removeMoney(
                playerId,
                amount,
                MoneyChangeReason.BANK_DEPOSIT
        );

        final int beforeBalance =
                bankRepository.findBalance(playerId);

        final int afterBalance =
                Math.addExact(
                        beforeBalance,
                        amount
                );

        bankRepository.setBalance(
                playerId,
                amount
        );

        return afterBalance;
    }

    /**
     * プレイヤーUUIDを検証します。
     *
     * @param playerId プレイヤーUUID
     */
    private void validatePlayerId(final UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );
    }

    /**
     * 金額を検証します。
     *
     * @param amount 金額
     */
    private void validateAmount(final int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero."
            );
        }
    }

    /**
     * 金額が正数であることを検証します。
     *
     * @param amount 検証対象の金額
     * @throws IllegalArgumentException amountが0以下の場合
     */
    private void validatePositiveAmount(final int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "amount must be greater than zero: " + amount
            );
        }
    }
}
