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
    private static final float SIZE = 0.3F;
    /** 투사체의 속력. 단위: 블록/s */
    protected int velocity;
    /** 중력 작용 여부 */
    protected boolean hasGravity;
    /** 투사체가 튕기는 횟수. {@code 0}으로 설정 시 튕기지 않음 */
    protected int bouncing;
    /** 투사체가 튕겼을 때의 속력 계수. {@link Projectile#bouncing}이 {@code 1} 이상이어야 함 */
    protected float bounceVelocityMultiplier;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 속력
     * @param option   선택적 옵션
     * @see ProjectileOption
     */
    protected Projectile(CombatEntity<?> shooter, int velocity, ProjectileOption option) {
        super(shooter, option.trailInterval, option.maxDistance, option.penetrating, option.hitboxMultiplier);
        this.velocity = velocity;
        this.hasGravity = option.hasGravity;
        this.bouncing = option.bouncing;
        this.bounceVelocityMultiplier = option.bounceVelocityMultiplier;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 속력
     */
    protected Projectile(CombatEntity<?> shooter, int velocity) {
        super(shooter);
        ProjectileOption option = ProjectileOption.builder().build();
        this.trailInterval = option.trailInterval;
        this.maxDistance = option.maxDistance;
        this.penetrating = option.penetrating;
        this.hitboxMultiplier = option.hitboxMultiplier;
        this.velocity = velocity;
        this.hasGravity = option.hasGravity;
        this.bouncing = option.bouncing;
        this.bounceVelocityMultiplier = option.bounceVelocityMultiplier;
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

        int loopCount = velocity / 5;
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
                    if (loc.distance(origin) >= maxDistance)
                        return false;

                    if (!LocationUtil.isNonSolid(loc)) {
                        handleBlockCollision(loc, finalDirection);
                        if (bouncing-- > 0)
                            handleBounce(loc, finalDirection);
                        else
                            return false;
                    }

                    if (loc.distance(origin) > MIN_DISTANCE && findEnemyAndHandleCollision(loc, targets, SIZE))
                        return false;

                    if (hasGravity) {
                        finalDirection.subtract(new Vector(0, 0.045 * ((float) loopCount / finalSum) / loopCount, 0));
                    }
                    loc.add(finalDirection);
                    if (count++ % trailInterval == 0)
                        trail(loc.clone());
                }

                return true;
            }
        };
    }

    /**
     * 투사체의 도탄 로직을 처리한다.
     *
     * @param location  위치
     * @param direction 발사 방향
     */
    private void handleBounce(Location location, Vector direction) {
        Location hitBlockLocation = location.getBlock().getLocation();
        Location beforeHitBlockLocation = location.clone().subtract(direction).getBlock().getLocation();
        Vector hitDir = hitBlockLocation.subtract(beforeHitBlockLocation).toVector();

        direction.multiply(bounceVelocityMultiplier);
        if (Math.abs(hitDir.getX()) > 0.5)
            direction.setX(-direction.getX());
        else if (Math.abs(hitDir.getY()) > 0.5)
            direction.setY(-direction.getY());
        else if (Math.abs(hitDir.getZ()) > 0.5)
            direction.setZ(-direction.getZ());
    }
}
