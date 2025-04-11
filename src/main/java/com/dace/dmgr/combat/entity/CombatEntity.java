package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.interaction.Hitbox;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.task.Task;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.function.LongConsumer;

/**
 * 전투 시스템의 엔티티 정보를 관리하는 인터페이스.
 *
 * @see AbstractCombatEntity
 */
public interface CombatEntity {
    /**
     * 지정한 월드에 있는 모든 전투 시스템 엔티티를 반환한다.
     *
     * @param world 대상 월드
     * @return 모든 전투 시스템 엔티티
     */
    @NonNull
    @UnmodifiableView
    static Collection<@NonNull CombatEntity> getAllExcluded(@NonNull World world) {
        return AbstractCombatEntity.getAllExcluded(world);
    }

    /**
     * 지정한 엔티티의 전투 시스템 엔티티 인스턴스를 반환한다.
     *
     * @param entity 대상 엔티티
     * @return 전투 시스템의 엔티티 인스턴스. 존재하지 않으면 {@code null} 반환
     */
    @Nullable
    static CombatEntity fromEntity(@NonNull Entity entity) {
        return AbstractCombatEntity.fromEntity(entity);
    }

    /**
     * @param <T> {@link Entity}를 상속받는 엔티티 타입
     * @return 엔티티 인스턴스
     */
    @NonNull
    <T extends Entity> T getEntity();

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
     * @return 팀. {@code null}이면 게임에 참여중이지 않음을 나타냄
     */
    @Nullable
    Game.Team getTeam();

    /**
     * 엔티티를 제거한다.
     *
     * @throws IllegalStateException 이미 제거되었으면 발생
     */
    void remove();

    /**
     * 엔티티가 제거되었는지 확인한다.
     *
     * @return 엔티티 제거 여부
     */
    boolean isRemoved();

    /**
     * 전투 시스템 엔티티가 실행하는 새로운 태스크를 추가한다.
     *
     * @param task 태스크
     * @throws IllegalStateException 해당 {@code task}가 이미 추가되었으면 발생
     */
    void addTask(@NonNull Task task);

    /**
     * 엔티티가 매 틱마다 실행할 작업을 추가한다.
     *
     * @param onTick 실행할 작업.
     *
     *               <p>인덱스 (0부터 시작)를 인자로 받음</p>
     */
    void addOnTick(@NonNull LongConsumer onTick);

    /**
     * 엔티티가 제거되었을 때 ({@link CombatEntity#remove()} 호출 시) 실행할 작업을 추가한다.
     *
     * @param onRemove 실행할 작업
     */
    void addOnRemove(@NonNull Runnable onRemove);

    /**
     * 엔티티의 너비를 반환한다.
     *
     * <p>스킬 판정 및 이펙트 등에 사용된다.</p>
     *
     * @return 너비 (단위: 블록)
     */
    double getWidth();

    /**
     * 엔티티의 높이를 반환한다.
     *
     * <p>스킬 판정 및 이펙트 등에 사용된다.</p>
     *
     * @return 높이 (단위: 블록)
     */
    double getHeight();

    /**
     * 엔티티의 히트박스 기준 중앙 위치를 반환한다.
     *
     * @return 히트박스 기준 중앙 위치
     */
    @NonNull
    Location getHitboxCenter();

    /**
     * 엔티티의 히트박스를 새로 설정한다.
     *
     * @param hitboxes 히트박스 목록
     */
    void setHitboxes(@NonNull Hitbox @NonNull ... hitboxes);

    /**
     * 지정한 위치의 구체와 접하고 있는 히트박스가 있는지 확인한다.
     *
     * @param location 확인할 위치
     * @param radius   판정 구체의 반지름. (단위: 블록). 0 이상의 값
     * @return 판정 구체가 히트박스와 접하고 있으면 {@code true} 반환
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Hitbox#isInHitbox(Location, double)
     */
    boolean isInHitbox(@NonNull Location location, double radius);

    /**
     * 엔티티의 현재 위치를 반환한다.
     *
     * @return 현재 위치
     */
    @NonNull
    Location getLocation();

    /**
     * 엔티티의 중심 위치를 반환한다.
     *
     * @return 엔티티의 중심 위치
     */
    @NonNull
    Location getCenterLocation();

    /**
     * 지정한 엔티티가 적인지 확인한다.
     *
     * <p>{@link CombatEntity} {@code a}와 {@code b}가 있을 때, 다음 조건을 반드시 충족해야 한다.</p>
     *
     * <ul>
     * <li>{@code a.isEnemy(a)}는 {@code false}이다.</li>
     * <li>{@code a.isEnemy(b)}와 {@code b.isEnemy(a)}는 같은 결과를 반환해야 한다.</li>
     * </ul>
     *
     * @param target 대상 엔티티
     * @return 적이면 {@code true} 반환
     */
    boolean isEnemy(@NonNull CombatEntity target);

    /**
     * 다른 엔티티가 이 엔티티를 대상으로 지정할 수 있는지 확인한다.
     *
     * @return 지정할 수 있으면 {@code true} 반환
     */
    boolean canBeTargeted();
}
