package com.dace.dmgr.combat;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
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
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 가장 가까운 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntities(Location, double, EntityCondition)
     */
    @Nullable
    public static <T extends CombatEntity> T getNearCombatEntity(@NonNull Location location, double range, @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return CombatEntity.getAllCombatEntities(location.getWorld()).stream()
                .map(entityCondition::cast)
                .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity) && combatEntity.canBeTargeted()
                        && combatEntity.isInHitbox(location, range))
                .findFirst()
                .orElse(null);
    }

    /**
     * 지정한 위치를 기준으로 범위 안의 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param location        위치
     * @param range           범위 (반지름). (단위: 블록). 0 이상의 값
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see CombatUtil#getNearCombatEntity(Location, double, EntityCondition)
     */
    @NonNull
    @UnmodifiableView
    public static <T extends CombatEntity> Set<@NonNull T> getNearCombatEntities(@NonNull Location location, double range,
                                                                                 @NonNull EntityCondition<T> entityCondition) {
        Validate.isTrue(range >= 0, "range >= 0 (%f)", range);

        return Collections.unmodifiableSet(
                CombatEntity.getAllCombatEntities(location.getWorld()).stream()
                        .map(entityCondition::cast)
                        .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity) && combatEntity.canBeTargeted()
                                && combatEntity.isInHitbox(location, range))
                        .collect(Collectors.toSet()));
    }

    /**
     * 지정한 월드에서 특정 조건을 만족하는 모든 엔티티를 반환한다.
     *
     * @param world           대상 월드
     * @param entityCondition 엔티티 탐색 조건
     * @param <T>             {@link CombatEntity}를 상속받는 전투 시스템 엔티티
     * @return 범위 내 모든 엔티티
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    @NonNull
    @UnmodifiableView
    public static <T extends CombatEntity> Set<@NonNull T> getCombatEntities(@NonNull World world, @NonNull EntityCondition<T> entityCondition) {
        return Collections.unmodifiableSet(
                CombatEntity.getAllCombatEntities(world).stream()
                        .map(entityCondition::cast)
                        .filter(combatEntity -> combatEntity != null && entityCondition.test(combatEntity))
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
     * 동시에 여러 총알을 발사한다.
     *
     * @param bulletFunction 발사할 총알 반환에 실행할 작업.
     *
     *                       <p>인덱스 (0부터 시작)를 인자로 받으며, 0번째 총알은 탄퍼짐 없이 발사됨</p>
     * @param amount         산탄 수. 2 이상의 값
     * @param spread         탄퍼짐. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void shotgun(@NonNull IntFunction<@NonNull Bullet<?>> bulletFunction, int amount, double spread) {
        Validate.isTrue(amount >= 2, "amount >= 0 (%f)", amount);
        Validate.isTrue(spread >= 0, "spread >= 0 (%f)", spread);

        bulletFunction.apply(0).shot();
        for (int i = 1; i < amount; i++) {
            Bullet<?> bullet = bulletFunction.apply(i);
            bullet.shot(VectorUtil.getSpreadedVector(bullet.getShooter().getLocation().getDirection(), spread));
        }
    }
}
