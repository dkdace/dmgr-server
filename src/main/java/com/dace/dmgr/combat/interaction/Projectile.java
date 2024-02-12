package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 투사체. 유한한 탄속을 가지는 총알을 관리하는 클래스.
 */
public abstract class Projectile extends Bullet {
    /** 기본 판정 범위. (단위: 블록) */
    private static final double SIZE = 0.13;
    /** 피해 증가량 */
    @Getter
    private final double damageIncrement;
    /** 투사체의 속력. (단위: 블록/s) */
    protected int velocity;
    /** 투사체가 유지되는 시간 (tick). {@code -1}로 설정 시 무한 지속 */
    protected long duration;
    /** 중력 작용 여부 */
    protected boolean hasGravity;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter  발사자
     * @param velocity 투사체의 속력. (단위: 블록/s)
     * @param option   선택적 옵션
     * @see ProjectileOption
     */
    protected Projectile(@NonNull CombatEntity shooter, int velocity, @NonNull ProjectileOption option) {
        super(shooter, option.trailInterval, option.maxDistance, option.hitboxMultiplier, option.condition);
        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사자
     * @param velocity 투사체의 속력. (단위: 블록/s)
     */
    protected Projectile(@NonNull CombatEntity shooter, int velocity) {
        super(shooter);
        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        ProjectileOption option = ProjectileOption.builder().build();
        this.trailInterval = option.trailInterval;
        this.maxDistance = option.maxDistance;
        this.hitboxMultiplier = option.hitboxMultiplier;
        this.condition = option.condition;
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도. (단위: ×0.01블록/블록)
     */
    @Override
    public final void shoot(@NonNull Location origin, @NonNull Vector direction, double spread) {
        direction.normalize();
        Location loc = origin.clone();
        loc.add(direction.clone().multiply(START_DISTANCE));
        direction = VectorUtil.getSpreadedVector(direction.multiply(HITBOX_INTERVAL), spread);
        Set<CombatEntity> targets = new HashSet<>();

        int loopCount = (int) (velocity / 2.5);
        int sum = 0;
        for (int i = 0; i <= loopCount; i++) {
            sum += i;
        }

        final Vector finalDirection = direction;
        final int finalSum = sum;

        TaskUtil.addTask(getShooter(), new IntervalTask(new Function<Long, Boolean>() {
            int count = 0;

            @Override
            public Boolean apply(Long i) {
                for (int j = 0; j < loopCount; j++) {
                    if ((duration != -1 && i >= duration) || loc.distance(origin) > maxDistance)
                        return false;

                    if (!LocationUtil.isNonSolid(loc) && !Projectile.this.handleBlockCollision(loc, finalDirection))
                        return false;

                    if (!Projectile.this.findEnemyAndHandleCollision(loc, finalDirection, targets, SIZE, condition))
                        return false;

                    if (hasGravity && LocationUtil.isNonSolid(loc.clone().subtract(0, 0.1, 0)))
                        finalDirection.subtract(new Vector(0, 0.045 * ((double) loopCount / finalSum) / loopCount, 0));

                    if (finalDirection.length() > 0.01)
                        loc.add(finalDirection);
                    if (count++ % trailInterval == 0)
                        Projectile.this.trail(loc.clone());
                }

                return true;
            }
        }, isCancelled -> onDestroy(loc.clone()), 1));
    }
}
