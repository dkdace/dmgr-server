package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

/**
 * 투사체. 유한한 탄속을 가지는 총알을 관리하는 클래스.
 */
public abstract class Projectile extends Bullet {
    /** 투사체의 기본 판정 범위. 단위: 블록 */
    private static final float SIZE = 0.13F;
    /** 투사체의 속력. 단위: 블록/s */
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
     * @param shooter  발사하는 엔티티
     * @param velocity 투사체의 속력. 단위: 블록/s
     * @param option   선택적 옵션
     * @see ProjectileOption
     */
    protected Projectile(CombatEntity<?> shooter, int velocity, ProjectileOption option) {
        super(shooter, option.trailInterval, option.maxDistance, option.penetrating, option.hitboxMultiplier);
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 투사체의 속력. 단위: 블록/s
     */
    protected Projectile(CombatEntity<?> shooter, int velocity) {
        super(shooter);
        ProjectileOption option = ProjectileOption.builder().build();
        this.trailInterval = option.trailInterval;
        this.maxDistance = option.maxDistance;
        this.penetrating = option.penetrating;
        this.hitboxMultiplier = option.hitboxMultiplier;
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체를 발사한다.
     *
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    @Override
    public final void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<CombatEntity<?>> targets = new HashSet<>();

        int loopCount = (int) (velocity / 2.5);
        int sum = 0;
        for (int i = 0; i <= loopCount; i++) {
            sum += i;
        }

        final Vector finalDirection = direction;
        final int finalSum = sum;

        new TaskTimer(1) {
            int count = 0;

            @Override
            public boolean run(int _i) {
                for (int i = 0; i < loopCount; i++) {
                    if (loc.distance(origin) >= maxDistance || (duration != -1 && _i >= duration))
                        return false;

                    if (!LocationUtil.isNonSolid(loc) && !handleBlockCollision(loc, finalDirection))
                        return false;

                    if (loc.distance(origin) > MIN_DISTANCE && !findEnemyAndHandleCollision(loc, finalDirection, targets, SIZE))
                        return false;

                    if (hasGravity && LocationUtil.isNonSolid(loc.clone().subtract(0, 0.1, 0)))
                        finalDirection.subtract(new Vector(0, 0.045 * ((float) loopCount / finalSum) / loopCount, 0));

                    if (finalDirection.length() > 0.01)
                        loc.add(finalDirection);
                    if (count++ % trailInterval == 0)
                        trail(loc.clone());
                }

                return true;
            }

            @Override
            public void onEnd(boolean cancelled) {
                onDestroy(loc.clone());
            }
        };
    }
}
