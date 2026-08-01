package com.example.rpg.player.status.model;

import java.util.Objects;
import java.util.UUID;

/**
 * プレイヤーのRPGステータスを管理するドメインモデル
 *
 * <p>
 * プレイヤー固有の基礎能力値、レベル、経験値、
 * 現在のHP・MP・スタミナ・シールドを保持する
 * </p>
 *
 * <p>
 * BaseStatusおよびResourceStatusは不変オブジェクト。
 * ステータス変更時は新しいインスタンスへ差し替える。
 * </p>
 */
public final class PlayerStatus {

    /**
     * プレイヤーUUID
     */
    private final UUID playerId;

    /**
     * 現在レベル
     */
    private int level;

    /**
     * 現在経験値
     */
    private long experience;

    /**
     * 基礎能力値
     */
    private BaseStatus baseStatus;

    /**
     * 現在のHP・MP・スタミナ・シールド
     */
    private ResourceStatus resourceStatus;

    /**
     * PlayerStatusを生成します。
     *
     * @param playerId       プレイヤーUUID
     * @param level          現在レベル
     * @param experience     現在経験値
     * @param baseStatus     基礎能力値
     * @param resourceStatus 現在リソース
     * @throws NullPointerException     playerId、baseStatus、resourceStatusのいずれかがnullの場合
     * @throws IllegalArgumentException levelが1未満、またはexperienceが負数の場合
     */
    public PlayerStatus(
            final UUID playerId,
            final int level,
            final long experience,
            final BaseStatus baseStatus,
            final ResourceStatus resourceStatus
    ) {
        this.playerId = Objects.requireNonNull(
                playerId,
                "playerId must not be null"
        );

        this.baseStatus = Objects.requireNonNull(
                baseStatus,
                "baseStatus must not be null"
        );

        this.resourceStatus = Objects.requireNonNull(
                resourceStatus,
                "resourceStatus must not be null"
        );

        validateLevel(level);
        validateExperience(experience);

        this.level = level;
        this.experience = experience;
    }

    /**
     * レベルが1以上であることを検証します。
     *
     * @param level 検証対象レベル
     */
    private static void validateLevel(final int level) {
        if (level < 1) {
            throw new IllegalArgumentException(
                    "level must be at least 1: " + level
            );
        }
    }

    /**
     * 経験値が0以上であることを検証します。
     *
     * @param experience 検証対象経験値
     */
    private static void validateExperience(
            final long experience
    ) {
        if (experience < 0) {
            throw new IllegalArgumentException(
                    "experience must not be negative: "
                            + experience
            );
        }
    }

    /**
     * プレイヤーUUIDを取得します。
     *
     * @return プレイヤーUUID
     */
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * 現在レベルを取得します。
     *
     * @return 現在レベル
     */
    public int getLevel() {
        return level;
    }

    /**
     * 現在経験値を取得します。
     *
     * @return 現在経験値
     */
    public long getExperience() {
        return experience;
    }

    /**
     * 基礎能力値を取得します。
     *
     * @return 基礎能力値
     */
    public BaseStatus getBaseStatus() {
        return baseStatus;
    }

    /**
     * 現在リソースを取得します。
     *
     * @return 現在リソース
     */
    public ResourceStatus getResourceStatus() {
        return resourceStatus;
    }

    /**
     * レベルを変更します。
     *
     * @param newLevel 新しいレベル
     * @throws IllegalArgumentException newLevelが1未満の場合
     */
    public void changeLevel(final int newLevel) {
        validateLevel(newLevel);
        this.level = newLevel;
    }

    /**
     * 経験値を変更します。
     *
     * @param newExperience 新しい経験値
     * @throws IllegalArgumentException newExperienceが負数の場合
     */
    public void changeExperience(final long newExperience) {
        validateExperience(newExperience);
        this.experience = newExperience;
    }

    /**
     * 基礎能力値を差し替えます。
     *
     * @param newBaseStatus 新しい基礎能力値
     * @throws NullPointerException newBaseStatusがnullの場合
     */
    public void replaceBaseStatus(
            final BaseStatus newBaseStatus
    ) {
        this.baseStatus = Objects.requireNonNull(
                newBaseStatus,
                "newBaseStatus must not be null"
        );
    }

    /**
     * 現在リソースを差し替えます。
     *
     * @param newResourceStatus 新しい現在リソース
     * @throws NullPointerException newResourceStatusがnullの場合
     */
    public void replaceResourceStatus(
            final ResourceStatus newResourceStatus
    ) {
        this.resourceStatus = Objects.requireNonNull(
                newResourceStatus,
                "newResourceStatus must not be null"
        );
    }
}