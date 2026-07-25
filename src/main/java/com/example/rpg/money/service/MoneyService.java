package com.example.rpg.money.service;

import com.example.rpg.event.publisher.BusinessEventPublisher;
import com.example.rpg.money.event.MoneyChangeReason;
import com.example.rpg.money.event.MoneyChangedEvent;
import com.example.rpg.money.exception.InsufficientMoneyException;
import com.example.rpg.repository.interfaces.IMoneyRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * プレイヤーの所持金に関する操作を提供するサービスです。
 *
 * <p>所持金の参照・加算・減算・設定は、このサービスを経由して行います。
 * 呼び出し側がMoneyRepositoryを直接操作することは想定していません。</p>
 */
public class MoneyService {

    /**
     * 所持金Repository
     */
    private final IMoneyRepository moneyRepository;

    /**
     * ビジネスイベント発行クラス
     */
    private final BusinessEventPublisher eventPublisher;

    /**
     * MoneyServiceを生成します。
     *
     * @param moneyRepository 所持金Repository
     * @param eventPublisher  ビジネスイベント発行クラス
     * @throws NullPointerException 引数がnullの場合
     */
    public MoneyService(
            IMoneyRepository moneyRepository,
            BusinessEventPublisher eventPublisher
    ) {
        this.moneyRepository = Objects.requireNonNull(
                moneyRepository,
                "moneyRepository must not be null"
        );

        this.eventPublisher = Objects.requireNonNull(
                eventPublisher,
                "eventPublisher must not be null"
        );
    }

    /**
     * プレイヤーの現在の所持金を取得します。
     *
     * @param playerId プレイヤーID
     * @return 現在の所持金
     */
    public int getBalance(final UUID playerId) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        return moneyRepository.findMoney(playerId);
    }

    /**
     * プレイヤーの所持金を加算します。
     *
     * @param playerId プレイヤーID
     * @param amount   加算額
     * @param reason   変更理由
     * @return 変更後の所持金
     */
    public int addMoney(
            final UUID playerId,
            final int amount,
            final MoneyChangeReason reason
    ) {
        validatePositiveAmount(amount);

        final int beforeMoney = getBalance(playerId);
        final int afterMoney = Math.addExact(beforeMoney, amount);

        updateBalance(
                playerId,
                beforeMoney,
                afterMoney,
                reason
        );

        return afterMoney;
    }

    /**
     * プレイヤーの所持金を減算します。
     *
     * @param playerId プレイヤーUUID
     * @param amount   減算額
     * @param reason   変更理由
     * @return 変更後の所持金
     * @throws IllegalArgumentException   amountが0以下の場合
     * @throws InsufficientMoneyException 所持金が不足している場合
     */
    public int removeMoney(
            final UUID playerId,
            final int amount,
            final MoneyChangeReason reason
    ) {
        validatePositiveAmount(amount);

        final int beforeMoney = getBalance(playerId);

        if (beforeMoney < amount) {
            throw new InsufficientMoneyException(
                    playerId,
                    beforeMoney,
                    amount
            );
        }

        final int afterMoney = beforeMoney - amount;

        updateBalance(
                playerId,
                beforeMoney,
                afterMoney,
                reason
        );

        return afterMoney;
    }

    /**
     * プレイヤーの所持金を指定額へ変更します。
     *
     * @param playerId プレイヤーUUID
     * @param amount   設定する所持金
     * @param reason   変更理由
     * @return 変更後の所持金
     * @throws IllegalArgumentException amountが負数の場合
     */
    public int set(
            final UUID playerId,
            final int amount,
            final MoneyChangeReason reason
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        if (amount < 0) {
            throw new IllegalArgumentException(
                    "amount must not be negative: " + amount
            );
        }

        final int beforeMoney = getBalance(playerId);

        updateBalance(
                playerId,
                beforeMoney,
                amount,
                reason
        );

        return amount;
    }

    /**
     * 送金元プレイヤーから送金先プレイヤーへ所持金を移動します。
     *
     * <p>
     * 現在のYAML Repositoryではトランザクションを利用できないため、
     * 送金元の減算後に送金先の加算が失敗すると、
     * 所持金データに不整合が発生する可能性があります。
     * 将来データベースへ移行する際は、送金処理をトランザクション化します。
     * </p>
     *
     * @param senderId   送金元プレイヤーID
     * @param receiverId 送金先プレイヤーID
     * @param amount     送金額
     * @throws NullPointerException       senderIdまたはreceiverIdがnullの場合
     * @throws IllegalArgumentException   同一プレイヤーへの送金、またはamountが0以下の場合
     * @throws InsufficientMoneyException 送金元の所持金が不足している場合
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

        validatePositiveAmount(amount);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "senderId and receiverId must be different"
            );
        }

        removeMoney(
                senderId,
                amount,
                MoneyChangeReason.PLAYER_PAYMENT_SENT
        );

        addMoney(
                receiverId,
                amount,
                MoneyChangeReason.PLAYER_PAYMENT_RECEIVED
        );
    }

    /**
     * Repositoryの所持金を更新し、所持金変更イベントを発行します。
     *
     * @param playerId    プレイヤーID
     * @param beforeMoney 変更前の所持金
     * @param afterMoney  変更後の所持金
     * @param reason      変更理由
     */
    private void updateBalance(
            final UUID playerId,
            final int beforeMoney,
            final int afterMoney,
            final MoneyChangeReason reason
    ) {
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        moneyRepository.setMoney(
                playerId,
                afterMoney
        );

        eventPublisher.publish(
                new MoneyChangedEvent(
                        playerId,
                        beforeMoney,
                        afterMoney,
                        reason
                )
        );
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
