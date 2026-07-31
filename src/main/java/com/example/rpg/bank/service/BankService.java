package com.example.rpg.bank.service;

import com.example.rpg.bank.event.BankBalanceChangeReason;
import com.example.rpg.bank.event.BankBalanceChangedEvent;
import com.example.rpg.bank.exception.InsufficientBankBalanceException;
import com.example.rpg.bank.repository.IBankRepository;
import com.example.rpg.event.publisher.BusinessEventPublisher;
import com.example.rpg.money.event.MoneyChangeReason;
import com.example.rpg.money.service.MoneyService;

import java.util.Map;
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
     * @param playerId 入金するプレイヤーのUUID
     * @param amount   入金額
     * @return 入金後の銀行残高
     */
    public int deposit(
            final UUID playerId,
            final int amount
    ) {
        validatePlayerId(playerId);

        validatePositiveAmount(amount);

        final int beforeBalance =
                bankRepository.findBalance(playerId);

        final int afterBalance =
                Math.addExact(
                        beforeBalance,
                        amount
                );

        moneyService.removeMoney(
                playerId,
                amount,
                MoneyChangeReason.BANK_DEPOSIT
        );

        updateBalance(
                playerId,
                beforeBalance,
                afterBalance,
                BankBalanceChangeReason.DEPOSIT
        );

        return afterBalance;
    }

    /**
     * 銀行残高から手持ち所持金へ出金します。
     *
     * @param playerId 出金するプレイヤーのUUID
     * @param amount   出金額
     * @return 出金後の銀行残高
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

        final int beforeMoney = moneyService.getBalance(playerId);

        // 銀行残高を変更する前に所持金のオーバーフローを検知する
        Math.addExact(
                beforeMoney,
                amount
        );

        final int afterBalance = beforeBalance - amount;

        updateBalance(
                playerId,
                beforeBalance,
                afterBalance,
                BankBalanceChangeReason.WITHDRAW
        );

        // MoneyServiceがMoneyChangedEventを発行する。
        moneyService.addMoney(playerId, amount, MoneyChangeReason.BANK_WITHDRAW);

        return afterBalance;
    }

    /**
     * 送金元プレイヤーの銀行残高から、
     * 送金先プレイヤーの銀行残高へ送金します。
     *
     * @param senderId   送金元プレイヤーのUUID
     * @param receiverId 送金先プレイヤーのUUID
     * @param amount     送金額
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

        final int beforeSenderBalance = bankRepository.findBalance(senderId);

        if (beforeSenderBalance < amount) {
            throw new InsufficientBankBalanceException(
                    senderId,
                    beforeSenderBalance,
                    amount
            );
        }

        final int beforeReceiverBalance = bankRepository.findBalance(receiverId);

        final int afterSenderBalance = beforeSenderBalance - amount;

        final int afterReceiverBalance = Math.addExact(beforeReceiverBalance, amount);

        // 送金元と送金先をまとめて更新し、保存を1回に限定する
        bankRepository.setBalances(
                Map.of(
                        senderId,
                        afterSenderBalance,
                        receiverId,
                        afterReceiverBalance
                )
        );

        // Repository更新成功後に送金元と送金先の銀行残高変更イベントを発行する
        publishBalanceChanged(
                senderId,
                beforeSenderBalance,
                afterSenderBalance,
                BankBalanceChangeReason.TRANSFER_SENT
        );

        publishBalanceChanged(
                receiverId,
                beforeReceiverBalance,
                afterReceiverBalance,
                BankBalanceChangeReason.TRANSFER_RECEIVED
        );
    }

    /**
     * 銀行残高を更新し、変更イベントを発行する
     *
     * @param playerId      プレイヤーUUID
     * @param beforeBalance 変更前銀行残高
     * @param afterBalance  変更後銀行残高
     * @param reason        変更理由
     */
    private void updateBalance(
            final UUID playerId,
            final int beforeBalance,
            final int afterBalance,
            final BankBalanceChangeReason reason
    ) {
        bankRepository.setBalance(
                playerId,
                afterBalance
        );

        publishBalanceChanged(
                playerId,
                beforeBalance,
                afterBalance,
                reason
        );
    }

    /**
     * 銀行残高変更イベントを発行します。
     *
     * @param playerId     対象プレイヤーUUID
     * @param beforeAmount 変更前残高
     * @param afterAmount  変更後残高
     * @param reason       変更理由
     */
    private void publishBalanceChanged(
            final UUID playerId,
            final int beforeAmount,
            final int afterAmount,
            final BankBalanceChangeReason reason
    ) {
        eventPublisher.publish(
                new BankBalanceChangedEvent(
                        playerId,
                        beforeAmount,
                        afterAmount,
                        reason
                )
        );
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
