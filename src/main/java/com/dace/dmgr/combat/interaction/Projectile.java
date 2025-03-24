package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Healer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

/**
 * 투사체. 유한한 탄속을 가져 매 틱마다 판정이 발생하는 총알을 관리하는 클래스.
 *
 * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
 */
public abstract class Projectile<T extends CombatEntity> extends Bullet<T> {
    /** 반복 횟수 */
    private final int loopCount;
    /** 투사체가 유지되는 시간 */
    private final Timespan duration;
    /** 발사자가 사용한 동작 */
    @Nullable
    private final Action action;
    /** 피해 증가량 */
    @Getter
    private final double damageIncrement;
    /** 치유 증가량 */
    @Getter
    private final double healIncrement;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param shooter         발사자
     * @param action          발사자가 사용한 동작
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @param option          투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Option
     */
    Projectile(@NonNull CombatEntity shooter, @Nullable Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition,
               @NonNull Option option) {
        super(shooter, option.startDistance, option.maxDistance, option.size, entityCondition);
        Validate.isTrue(speed >= 0, "speed >= 0 (%d)", speed);

        this.duration = option.duration;
        this.loopCount = (int) (speed / (20.0 / (1.0 / HITBOX_INTERVAL)));
        this.action = action;
        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.healIncrement = (shooter instanceof Healer) ? ((Healer) shooter).getHealerModule().getHealMultiplierStatus().getValue() : 1;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param shooter         발사자
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @param option          투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Option
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition, @NonNull Option option) {
        this(shooter, null, speed, entityCondition, option);
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link Option}을 통해 전달받는다.</p>
     *
     * @param action          발사자가 사용한 동작
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @param option          투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see Option
     */
    protected Projectile(@NonNull Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition, @NonNull Option option) {
        this(action.getCombatUser(), action, speed, entityCondition, option);
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter         발사자
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        this(shooter, null, speed, entityCondition, Option.builder().build());
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param action          발사자가 사용한 동작
     * @param speed           투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param entityCondition 대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Projectile(@NonNull Action action, int speed, @NonNull CombatUtil.EntityCondition<T> entityCondition) {
        this(action.getCombatUser(), action, speed, entityCondition, Option.builder().build());
    }

    @Override
    final void onShot() {
        IntervalTask onTickTask = new IntervalTask(i -> {
            for (int j = 0; j < loopCount; j++) {
                next();
                if (isDestroyed())
                    return false;
            }

            return (duration == Timespan.MAX || i < duration.toTicks()) && getDistanceFromStart() < maxDistance;
        }, () -> {
            if (!isDestroyed())
                destroy();
        }, 1);

        if (action != null)
            action.addTask(onTickTask);
    }

    /**
     * 중력 효과를 적용하는 판정점 처리기를 생성한다.
     *
     * @return 판정점 처리기
     */
    @NonNull
    protected final IntervalHandler createGravityIntervalHandler() {
        int sum = IntStream.rangeClosed(0, loopCount).sum();

        return (location, i) -> {
            if (LocationUtil.isNonSolid(getLocation().subtract(0, HITBOX_INTERVAL, 0)))
                push(new Vector(0, -(0.045 * ((double) loopCount / sum) / loopCount), 0));

            return true;
        };
    }

    /**
     * 지면 고정 효과를 적용하는 판정점 처리기를 생성한다.
     *
     * @return 판정점 처리기
     */
    @NonNull
    protected final IntervalHandler createGroundIntervalHandler() {
        return (location, i) -> {
            if (!LocationUtil.isNonSolid(location)) {
                Location up = LocationUtil.getNearestAgainstEdge(location, new Vector(0, 1, 0), 2.5);
                if (LocationUtil.isNonSolid(up)) {
                    move(up);
                    return true;
                }

                return false;
            } else if (LocationUtil.isNonSolid(location.clone().subtract(0, HITBOX_INTERVAL, 0))) {
                Location down = LocationUtil.getNearestAgainstEdge(location, new Vector(0, -1, 0), 2.5);
                if (LocationUtil.isNonSolid(down) && !LocationUtil.isNonSolid(down.clone().subtract(0, HITBOX_INTERVAL, 0))) {
                    move(down);
                    return true;
                }

                return false;
            }

            return true;
        };
    }

    /**
     * 투사체의 선택적 옵션을 관리하는 클래스.
     */
    @Builder
    public static final class Option {
        /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록). 0 이상의 값 */
        @Builder.Default
        private final double startDistance = 0.5;
        /** 총알의 최대 사거리. (단위: 블록). {@code startDistance} 이상의 값 */
        @Builder.Default
        private final double maxDistance = 70;
        /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록). 0 이상의 값 */
        @Builder.Default
        private final double size = 0.13;
        /** 투사체가 유지되는 시간 */
        @Builder.Default
        @NonNull
        private final Timespan duration = Timespan.MAX;
    }
}
