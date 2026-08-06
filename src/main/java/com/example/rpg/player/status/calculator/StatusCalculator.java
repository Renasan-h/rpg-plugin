package com.example.rpg.player.status.calculator;

import com.example.rpg.player.status.model.BaseStatus;
import com.example.rpg.player.status.model.FinalStatus;
import com.example.rpg.player.status.modifier.ModifierOperation;
import com.example.rpg.player.status.modifier.StatusModifier;
import com.example.rpg.player.status.modifier.StatusType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基礎ステータスと補正値から最終ステータスを計算する
 *
 * <p>
 * 固定値補正と割合補正をそれぞれ合算した後、
 * 次の式で最終値を計算する
 * </p>
 *
 * <pre>
 * finalValue
 *     = (baseValue + flatSum)
 *     * (RATE_SCALE + rateSum)
 *     / RATE_SCALE
 * </pre>
 *
 * <p>
 * 補正値の取得順や登録順によって、
 * 計算結果が変化しない設計
 * </p>
 */
public final class StatusCalculator {

    /**
     * 基礎ステータスと補正値から最終ステータスを生成します。
     *
     * @param baseStatus 基礎ステータス
     * @param modifiers  適用するステータス補正
     * @return 計算後の最終ステータス
     * @throws NullPointerException baseStatus、modifiers、または要素がnullの場合
     * @throws ArithmeticException  計算結果がintの範囲を超えた場合
     */
    public FinalStatus calculate(
            final BaseStatus baseStatus,
            final List<StatusModifier> modifiers
    ) {
        Objects.requireNonNull(
                baseStatus,
                "baseStatus must not be null"
        );

        Objects.requireNonNull(
                modifiers,
                "modifiers must not be null"
        );

        final Map<StatusType, ModifierSummary> summaries =
                summarizeModifiers(modifiers);

        return new FinalStatus(
                calculateValue(
                        baseStatus.getHp(),
                        summaries.get(StatusType.HP)
                ),
                calculateValue(
                        baseStatus.getMp(),
                        summaries.get(StatusType.MP)
                ),
                calculateValue(
                        baseStatus.getStamina(),
                        summaries.get(StatusType.STAMINA)
                ),
                calculateValue(
                        baseStatus.getAttack(),
                        summaries.get(StatusType.ATTACK)
                ),
                calculateValue(
                        baseStatus.getMagicAttack(),
                        summaries.get(StatusType.MAGIC_ATTACK)
                ),
                calculateValue(
                        baseStatus.getDefense(),
                        summaries.get(StatusType.DEFENSE)
                ),
                calculateValue(
                        baseStatus.getMagicDefense(),
                        summaries.get(StatusType.MAGIC_DEFENSE)
                ),
                calculateValue(
                        baseStatus.getStrength(),
                        summaries.get(StatusType.STRENGTH)
                ),
                calculateValue(
                        baseStatus.getDexterity(),
                        summaries.get(StatusType.DEXTERITY)
                ),
                calculateValue(
                        baseStatus.getVitality(),
                        summaries.get(StatusType.VITALITY)
                ),
                calculateValue(
                        baseStatus.getIntelligence(),
                        summaries.get(StatusType.INTELLIGENCE)
                ),
                calculateValue(
                        baseStatus.getLuck(),
                        summaries.get(StatusType.LUCK)
                ),
                calculateValue(
                        0,
                        summaries.get(StatusType.CRITICAL_RATE)
                ),
                calculateValue(
                        FinalStatus.RATE_SCALE,
                        summaries.get(StatusType.CRITICAL_DAMAGE)
                ),
                calculateValue(
                        FinalStatus.RATE_SCALE,
                        summaries.get(StatusType.ACCURACY)
                ),
                calculateValue(
                        0,
                        summaries.get(StatusType.EVASION)
                ),
                calculateValue(
                        FinalStatus.RATE_SCALE,
                        summaries.get(StatusType.ATTACK_SPEED)
                ),
                calculateValue(
                        FinalStatus.RATE_SCALE,
                        summaries.get(StatusType.MOVE_SPEED)
                )
        );
    }

    /**
     * ステータス補正を対象ステータスごとに集計します。
     *
     * @param modifiers ステータス補正一覧
     * @return ステータスごとの補正集計結果
     */
    private Map<StatusType, ModifierSummary> summarizeModifiers(
            final List<StatusModifier> modifiers
    ) {
        final Map<StatusType, ModifierSummary> summaries =
                new EnumMap<>(StatusType.class);

        for (final StatusType statusType : StatusType.values()) {
            summaries.put(
                    statusType,
                    new ModifierSummary()
            );
        }

        for (final StatusModifier modifier : modifiers) {
            Objects.requireNonNull(
                    modifier,
                    "modifier must not be null"
            );

            final ModifierSummary summary =
                    summaries.get(
                            modifier.getStatusType()
                    );

            summary.add(
                    modifier.getOperation(),
                    modifier.getValue()
            );
        }

        return summaries;
    }

    /**
     * 基礎値へ固定値補正と割合補正を適用します。
     *
     * @param baseValue 基礎値
     * @param summary   補正集計結果
     * @return 最終値
     */
    private int calculateValue(
            final int baseValue,
            final ModifierSummary summary
    ) {
        final long valueAfterAddition =
                Math.addExact(
                        baseValue,
                        summary.getFlatSum()
                );

        final long effectiveRate =
                Math.addExact(
                        FinalStatus.RATE_SCALE,
                        summary.getRateSum()
                );

        /*
         * 割合補正の合計によって計算結果が負数になった場合は、
         * 最終ステータスの最小値である0へ丸める。
         */
        if (effectiveRate <= 0) {
            return 0;
        }

        final long multipliedValue =
                Math.multiplyExact(
                        valueAfterAddition,
                        effectiveRate
                );

        final long calculatedValue =
                multipliedValue
                        / FinalStatus.RATE_SCALE;

        if (calculatedValue <= 0) {
            return 0;
        }

        return Math.toIntExact(calculatedValue);
    }

    /**
     * 1種類のステータスに対する補正値を集計します。
     */
    private static final class ModifierSummary {

        /**
         * 固定値補正の合計
         */
        private long flatSum;

        /**
         * 割合補正の合計
         */
        private long rateSum;

        /**
         * 補正値を演算種別に応じて加算します。
         *
         * @param operation 補正演算
         * @param value     補正値
         */
        private void add(
                final ModifierOperation operation,
                final int value
        ) {
            Objects.requireNonNull(
                    operation,
                    "operation must not be null"
            );

            switch (operation) {
                case ADD -> flatSum = Math.addExact(
                        flatSum,
                        value
                );

                case MULTIPLY -> rateSum = Math.addExact(
                        rateSum,
                        value
                );
            }
        }

        /**
         * 固定値補正の合計を取得します。
         *
         * @return 固定値補正合計
         */
        private long getFlatSum() {
            return flatSum;
        }

        /**
         * 割合補正の合計を取得します。
         *
         * @return 割合補正合計
         */
        private long getRateSum() {
            return rateSum;
        }
    }
}