package com.dace.dmgr.combat;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatUtil {
    /**
     * 지정한 피해량에 거리별 피해량 감소가 적용된 최종 피해량을 반환한다.
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // 최종 피해량 : 10 ~ 5 (20m~40m) = 5
     * double damage = getDistantDamage(10, 40, 20)
     * </code></pre>
     *
     * @param damage            피해량. 0 이상의 값
     * @param distance          거리 (단위: 블록). 0 이상의 값
     * @param weakeningDistance 피해 감소가 시작하는 거리. (단위: 블록). 0 이상의 값
     * @return 최종 피해량
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static double getDistantDamage(double damage, double distance, double weakeningDistance) {
        Validate.isTrue(damage >= 0, "damage >= 0 (%f)", damage);
        Validate.isTrue(distance >= 0, "distance >= 0 (%f)", distance);
        Validate.isTrue(weakeningDistance >= 0, "weakeningDistance >= 0 (%f)", weakeningDistance);

        if (distance <= weakeningDistance)
            return damage;

        double halfDamage = damage / 2.0;
        distance = distance - weakeningDistance;

        return Math.max(halfDamage, halfDamage * ((weakeningDistance - distance) / weakeningDistance) + halfDamage);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 가장 가까운 엔티티를 반환한다.
     *
     * @param game            대상 게임. {@code null}로 지정 시 게임에 소속되지 않은 엔티티 ({@link CombatEntity#getAllExcluded()})를 대상으로 함
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 가장 가까운 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntities(Game, Location, double, EntityCondition)
     */
    @Nullable
    public static <T extends CombatEntity> T getNearCombatEntity(@Nullable Game game, @NonNull Location location, double range,
                                                                 @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return (game == null ? CombatEntity.getAllExcluded() : game.getCombatEntities()).stream()
                .map(combatEntity -> entityCondition.targetClass.isInstance(combatEntity)
                        ? entityCondition.targetClass.cast(combatEntity)
                        : null)
                .filter(combatEntity -> combatEntity != null
                        && entityCondition.targetCondition.test(combatEntity)
                        && combatEntity.canBeTargeted()
                        && combatEntity.isInHitbox(location, range))
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param game            대상 게임. {@code null}로 지정 시 게임에 소속되지 않은 엔티티 ({@link CombatEntity#getAllExcluded()})를 대상으로 함
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntity(Game, Location, double, EntityCondition)
     */
    @NonNull
    @UnmodifiableView
    public static <T extends CombatEntity> Set<@NonNull T> getNearCombatEntities(@Nullable Game game, @NonNull Location location, double range,
                                                                                 @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return Collections.unmodifiableSet(
                (game == null ? CombatEntity.getAllExcluded() : game.getCombatEntities()).stream()
                        .map(combatEntity -> entityCondition.targetClass.isInstance(combatEntity)
                                ? entityCondition.targetClass.cast(combatEntity)
                                : null)
                        .filter(combatEntity -> combatEntity != null
                                && entityCondition.targetCondition.test(combatEntity)
                                && combatEntity.canBeTargeted()
                                && combatEntity.isInHitbox(location, range))
                        .collect(Collectors.toSet()));
    }

    /**
     * 지정한 플레이어에게 화면 반동 효과를 전송한다.
     *
     * <p>주로 총기 반동에 사용된다.</p>
     *
     * @param combatUser      대상 플레이어
     * @param up              수직 반동
     * @param side            수평 반동
     * @param upSpread        수직 반동 분산도
     * @param sideSpread      수평 반동 분산도
     * @param duration        반동 진행 시간 (tick). 1~5 사이의 값
     * @param firstMultiplier 초탄 반동 계수. 1로 설정 시 차탄과 동일. 1 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void sendRecoil(@NonNull CombatUser combatUser, double up, double side, double upSpread, double sideSpread, int duration, double firstMultiplier) {
        Validate.inclusiveBetween(1, 5, duration, "5 >= duration >= 1 (%d)", duration);
        Validate.isTrue(firstMultiplier >= 1, "firstMultiplier >= 1 (%f)", firstMultiplier);

        double finalUpSpread = upSpread * (Math.random() - Math.random()) * 0.5;
        double finalSideSpread = sideSpread * (Math.random() - Math.random()) * 0.5;
        boolean first = combatUser.getWeaponFirstRecoilTimestamp().isBefore(Timestamp.now());
        int sum = IntStream.rangeClosed(1, duration).sum();
        combatUser.setWeaponFirstRecoilTimestamp(Timestamp.now().plus(Timespan.ofTicks(4)));

        combatUser.addTask(new IntervalTask(i -> {
            double finalUp = (up + finalUpSpread) / ((double) sum / (duration - i));
            double finalSide = (side + finalSideSpread) / ((double) sum / (duration - i));
            if (first) {
                finalUp *= firstMultiplier;
                finalSide *= firstMultiplier;
            }

            combatUser.addYawAndPitch(finalSide, -finalUp);
        }, 1, duration));
    }

    /**
     * 지정한 플레이어에게 화면 흔들림 효과를 전송한다.
     *
     * @param combatUser  대상 플레이어
     * @param yawSpread   Yaw 분산도
     * @param pitchSpread Pitch 분산도
     * @param duration    진행 시간
     * @see CombatUtil#sendShake(CombatUser, double, double)
     */
    public static void sendShake(@NonNull CombatUser combatUser, double yawSpread, double pitchSpread, @NonNull Timespan duration) {
        combatUser.addTask(new IntervalTask((LongConsumer) i -> sendShake(combatUser, yawSpread, pitchSpread), 1, duration.toTicks()));
    }

    /**
     * 지정한 플레이어에게 화면 흔들림 효과를 전송한다.
     *
     * @param combatUser  대상 플레이어
     * @param yawSpread   Yaw 분산도
     * @param pitchSpread Pitch 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void sendShake(@NonNull CombatUser combatUser, double yawSpread, double pitchSpread) {
        combatUser.addYawAndPitch(
                RandomUtils.nextDouble(0, yawSpread) - RandomUtils.nextDouble(0, yawSpread),
                RandomUtils.nextDouble(0, pitchSpread) - RandomUtils.nextDouble(0, pitchSpread));
    }

    /**
     * 전투 시스템의 엔티티 탐색 조건을 나타내는 클래스.
     *
     * @param <T> {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EntityCondition<T extends CombatEntity> {
        /** 대상 엔티티의 클래스 인스턴스 */
        private final Class<T> targetClass;
        /** 대상 엔티티를 찾는 조건 */
        private final Predicate<T> targetCondition;

        /**
         * 지정한 클래스와 조건으로 엔티티 탐색 조건을 생성한다.
         *
         * @param targetClass     대상 엔티티의 클래스 인스턴스
         * @param targetCondition 대상 엔티티를 찾는 조건
         * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
         * @return {@link EntityCondition}
         */
        @NonNull
        public static <T extends CombatEntity> EntityCondition<T> of(@NonNull Class<T> targetClass, @NonNull Predicate<T> targetCondition) {
            return new EntityCondition<>(targetClass, targetCondition);
        }

        /**
         * 지정한 클래스로 엔티티 탐색 조건을 생성한다.
         *
         * @param targetClass 대상 엔티티의 클래스 인스턴스
         * @param <T>         {@link CombatEntity}를 상속받는 전투 시스템 엔티티
         * @return {@link EntityCondition}
         */
        @NonNull
        public static <T extends CombatEntity> EntityCondition<T> of(@NonNull Class<T> targetClass) {
            return of(targetClass, combatEntity -> true);
        }

        /**
         * 모든 엔티티를 포함하는, 조건이 지정되지 않은 엔티티 탐색 조건을 생성한다.
         *
         * @return {@link EntityCondition}
         */
        @NonNull
        public static EntityCondition<@NonNull CombatEntity> all() {
            return of(CombatEntity.class);
        }

        /**
         * 지정한 엔티티의 적을 포함하는 엔티티 탐색 조건을 생성한다.
         *
         * @param target 대상 엔티티
         * @return {@link EntityCondition}
         * @see CombatEntity#isEnemy(CombatEntity)
         */
        @NonNull
        public static EntityCondition<@NonNull Damageable> enemy(@NonNull Damageable target) {
            return of(Damageable.class, combatEntity -> combatEntity.isEnemy(target));
        }

        /**
         * 지정한 엔티티의 아군을 포함하는 엔티티 탐색 조건을 생성한다.
         *
         * @param target 대상 엔티티
         * @return {@link EntityCondition}
         * @see CombatEntity#isEnemy(CombatEntity)
         */
        @NonNull
        public static EntityCondition<@NonNull Healable> team(@NonNull Healable target) {
            return of(Healable.class, combatEntity -> !combatEntity.isEnemy(target));
        }

        /**
         * 지정한 엔티티가 조건을 만족하는지 확인한다.
         *
         * @param target 대상 엔티티
         * @return 조건을 만족하면 {@code true} 반환
         */
        private boolean test(@NonNull CombatEntity target) {
            return targetClass.isInstance(target) && targetCondition.test(targetClass.cast(target));
        }

        /**
         * 현재 조건과 지정한 조건을 모두 만족하는 AND 조건을 생성한다.
         *
         * @param targetCondition 대상 엔티티를 찾는 조건
         * @return {@link EntityCondition}
         */
        @NonNull
        public EntityCondition<T> and(@NonNull Predicate<T> targetCondition) {
            return of(targetClass, combatEntity -> this.targetCondition.test(combatEntity) && targetCondition.test(combatEntity));
        }

        /**
         * 현재 조건과 지정한 엔티티 탐색 조건을 모두 만족하는 AND 조건을 생성한다.
         *
         * @param entityCondition 엔티티 탐색 조건
         * @param <U>             {@link T} 또는 하위 엔티티 타입
         * @return {@link EntityCondition}
         */
        @NonNull
        public <U extends T> EntityCondition<U> and(@NonNull EntityCondition<U> entityCondition) {
            return entityCondition.and(entityCondition::test);
        }

        /**
         * 현재 조건 또는 지정한 조건을 만족하는 OR 조건을 생성한다.
         *
         * @param targetCondition 대상 엔티티를 찾는 조건
         * @return {@link EntityCondition}
         */
        @NonNull
        public EntityCondition<T> or(@NonNull Predicate<T> targetCondition) {
            return of(targetClass, combatEntity -> this.targetCondition.test(combatEntity) || targetCondition.test(combatEntity));
        }

        /**
         * 현재 조건 또는 지정한 엔티티 탐색 조건을 만족하는 OR 조건을 생성한다.
         *
         * @param entityCondition 엔티티 탐색 조건
         * @param <U>             {@link T} 또는 하위 엔티티 타입
         * @return {@link EntityCondition}
         */
        @NonNull
        public <U extends T> EntityCondition<T> or(@NonNull EntityCondition<U> entityCondition) {
            return or(entityCondition::test);
        }

        /**
         * 현재 조건에 대상 엔티티를 포함한다.
         *
         * <p>다음과 동일한 결과를 나타냄:</p>
         *
         * <pre><code>
         * EntityCondition.or(combatEntity -> combatEntity == target);
         * </code></pre>
         *
         * @param target 포함할 엔티티
         * @return {@link EntityCondition}
         */
        @NonNull
        public EntityCondition<T> include(@NonNull T target) {
            return or(combatEntity -> combatEntity == target);
        }

        /**
         * 현재 조건에서 대상 엔티티를 제외한다.
         *
         * <p>다음과 동일한 결과를 나타냄:</p>
         *
         * <pre><code>
         * EntityCondition.and(combatEntity -> combatEntity != target);
         * </code></pre>
         *
         * @param target 제외할 엔티티
         * @return {@link EntityCondition}
         */
        @NonNull
        public EntityCondition<T> exclude(@NonNull T target) {
            return and(combatEntity -> combatEntity != target);
        }
    }
}
