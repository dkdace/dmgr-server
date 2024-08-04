package com.dace.dmgr.combat.entity;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 인터페이스.
 *
 * @see AbstractCombatEntity
 */
public interface CombatEntity extends Disposable {
    /**
     * 게임에 소속되지 않은 모든 엔티티를 반환한다.
     *
     * @return 게임에 소속되지 않은 모든 엔티티
     */
    @NonNull
    static CombatEntity @NonNull [] getAllExcluded() {
        return CombatEntityRegistry.getInstance().getAllExcluded();
    }

    /**
     * 지정한 엔티티의 전투 시스템 엔티티 인스턴스를 반환한다.
     *
     * @param entity 대상 엔티티
     * @return 전투 시스템의 엔티티 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    static CombatEntity fromEntity(@NonNull Entity entity) {
        return CombatEntityRegistry.getInstance().get(entity);
    }

    /**
     * @param <T> {@link Entity}를 상속받는 엔티티 타입
     * @return 엔티티 객체
     */
    @NonNull
    <T extends Entity> T getEntity();

    /**
     * @return 속성 목록 관리 객체
     */
    @NonNull
    PropertyManager getPropertyManager();

    /**
     * @return 히트박스 객체 목록
     */
    @NonNull
    Hitbox @NonNull [] getHitboxes();

    /**
     * 히트박스의 현재 중앙 위치를 반환한다.
     *
     * @return 히트박스의 중앙 위치
     */
    @NonNull
    Location getHitboxLocation();

    /**
     * 엔티티의 중심 위치를 반환한다.
     *
     * @return 엔티티의 중심 위치
     */
    @NonNull
    Location getCenterLocation();

    /**
     * 히트박스 목록에서 지정한 위치까지 가장 가까운 위치를 반환한다.
     *
     * @param location 대상 위치
     * @return 가장 가까운 위치
     */
    @NonNull
    Location getNearestLocationOfHitboxes(@NonNull Location location);

    /**
     * @return 이름
     */
    @NonNull
    String getName();

    /**
     * @return 소속된 게임. {@code null}이면 게임에 참여중이지 않음을 나타냄
     */
    @Nullable
    Game getGame();

    /**
     * 팀 식별자를 반환한다.
     *
     * <p>시스템에서 적과 아군을 구별하기 위해 사용한다.</p>
     *
     * @return 팀 식별자
     * @see CombatEntity#isEnemy(CombatEntity)
     */
    @NonNull
    String getTeamIdentifier();

    /**
     * @return 히트박스의 가능한 최대 크기. (단위: 블록)
     */
    double getMaxHitboxSize();

    /**
     * 엔티티가 활성화 되었는지 확인한다.
     *
     * @return 엔티티 활성화 여부
     */
    boolean isActivated();

    /**
     * 엔티티 활성화 작업을 수행한다.
     */
    void activate();

    /**
     * 지정한 엔티티가 적인 지 확인한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 적이면 {@code true} 반환
     * @see CombatEntity#getTeamIdentifier()
     */
    boolean isEnemy(@NonNull CombatEntity combatEntity);

    /**
     * 다른 엔티티가 이 엔티티를 대상으로 지정할 수 있는 지 확인한다.
     *
     * @return 지정할 수 있으면 {@code true} 반환
     */
    boolean canBeTargeted();
}
