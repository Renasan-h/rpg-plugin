package com.example.rpg.bank.service;

import com.example.rpg.bank.exception.InsufficientBankBalanceException;
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
        validatePlayerId(playerId);

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
     * 銀行残高から手持ち所持金へ出金します。
     *
     * <p>
     * 銀行残高が出金額未満の場合は、
     * {@link InsufficientBankBalanceException}を送出します。
     * 銀行残高の減算後、MoneyServiceを経由して
     * 手持ち所持金へ出金額を加算します。
     * </p>
     *
     * @param playerId 出金するプレイヤーのUUID
     * @param amount   出金額
     * @return 出金後の銀行残高
     * @throws NullPointerException             playerIdがnullの場合
     * @throws IllegalArgumentException         amountが0以下の場合
     * @throws InsufficientBankBalanceException 銀行残高が不足している場合
     * @throws ArithmeticException              手持ち所持金の加算時にオーバーフローした場合
     */
    public int withdraw(
            final UUID playerId,
            final int amount
    ) {
        validatePlayerId(playerId);

        validateAmount(amount);

        final int beforeBalance = bankRepository.findBalance(playerId);

        if (beforeBalance < amount) {
            throw new InsufficientBankBalanceException(
                    playerId,
                    beforeBalance,
                    amount
            );
        }

        final int afterBalance = beforeBalance - amount;

        bankRepository.setBalance(playerId, amount);

        moneyService.addMoney(playerId, amount, MoneyChangeReason.BANK_WITHDRAW);

        return afterBalance;
    }

    /**
     * 送金元プレイヤーの銀行残高から、
     * 送金先プレイヤーの銀行残高へ送金します。
     *
     * <p>
     * 送金先プレイヤーがオフラインの場合でも、
     * UUIDを指定できれば銀行残高へ自動的に加算されます。
     * </p>
     *
     * @param senderId   送金元プレイヤーのUUID
     * @param receiverId 送金先プレイヤーのUUID
     * @param amount     送金額
     * @throws NullPointerException             senderIdまたはreceiverIdがnullの場合
     * @throws IllegalArgumentException         amountが0以下の場合、または送金元と送金先が同一の場合
     * @throws InsufficientBankBalanceException 送金元の銀行残高が不足している場合
     * @throws ArithmeticException              送金先の銀行残高加算時にオーバーフローした場合
     */
    public void transfer(
            final UUID senderId,
            final UUID receiverId,
            final int amount
    ) {
        Objects.requireNonNull(
                senderId,
                "senderId must not be null"
        );

        Objects.requireNonNull(
                receiverId,
                "receiverId must not be null"
        );

        validateAmount(amount);

        if (receiverId.equals(senderId)) {
            throw new IllegalArgumentException(
                    "senderId and receiverId must be different"
            );
        }

        final int senderBalance = bankRepository.findBalance(senderId);

        if (senderBalance < amount) {
            throw new InsufficientBankBalanceException(
                    senderId,
                    senderBalance,
                    amount
            );
        }

        final int receiverBalance = bankRepository.findBalance(receiverId);

        final int updatedSenderBalance = senderBalance - amount;

        final int updatedReceiverBalance = Math.addExact(receiverBalance, amount);

        bankRepository.setBalance(senderId, updatedSenderBalance);
        bankRepository.setBalance(receiverId, updatedReceiverBalance);
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
