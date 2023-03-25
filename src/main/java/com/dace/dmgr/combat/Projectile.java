package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.task.TaskTimer;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Map;
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

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter       발사하는 엔티티
     * @param trailInterval 트레일 실행 주기
     * @param velocity      속력
     * @param option        선택적 옵션
     * @see ProjectileOption
     */
    public Projectile(CombatEntity<?> shooter, int trailInterval, int velocity, ProjectileOption option) {
        super(shooter, trailInterval, option.penetrating, option.hitboxMultiplier);
        this.velocity = velocity;
        this.hasGravity = option.hasGravity;
        this.bouncing = option.bouncing;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter       발사하는 엔티티
     * @param trailInterval 트레일 실행 주기
     * @param velocity      속력
     */
    public Projectile(CombatEntity<?> shooter, int trailInterval, int velocity) {
        super(shooter, trailInterval);
        ProjectileOption option = ProjectileOption.builder().build();
        this.velocity = velocity;
        this.hasGravity = option.hasGravity;
        this.bouncing = option.bouncing;
    }

    /**
     * 투사체를 발사한다.
     *
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    @Override
    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<CombatEntity<?>> targetList = new HashSet<>();

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
                    if (loc.distance(origin) >= MAX_RANGE)
                        return false;

                    Location hitLoc = loc.clone().add(finalDirection);
                    if (!LocationUtil.isNonSolid(hitLoc)) {
                        Vector subDir = finalDirection.clone().multiply(0.25);

                        while (LocationUtil.isNonSolid(loc))
                            loc.add(subDir);

                        loc.subtract(subDir);
                        onHit(loc);
                        onHitBlock(loc, hitLoc.getBlock());

                        if (bouncing-- > 0) {
                            Vector hitDir = hitLoc.getBlock().getLocation().subtract(loc.getBlock().getLocation()).toVector();

                            if (Math.abs(hitDir.getX()) > 0.5)
                                finalDirection.setX(-finalDirection.getX());
                            else if (Math.abs(hitDir.getY()) > 0.5)
                                finalDirection.setY(-finalDirection.getY());
                            else if (Math.abs(hitDir.getZ()) > 0.5)
                                finalDirection.setZ(-finalDirection.getZ());

                            return true;
                        } else
                            return false;
                    }

                    if (loc.distance(origin) > 0.5) {
                        Map.Entry<CombatEntity<?>, Boolean> targetEntry
                                = Combat.getNearEnemy(shooter, loc, SIZE * hitboxMultiplier);
                        CombatEntity<?> target = targetEntry.getKey();
                        boolean isCrit = targetEntry.getValue();

                        if (target != null) {
                            if (targetList.add(target)) {
                                onHit(hitLoc);
                                onHitEntity(hitLoc, target, isCrit);

                                if (!penetrating)
                                    return false;
                            }
                        }
                    }
                    if (hasGravity) {
                        finalDirection.subtract(new Vector(0, 0.045 * ((float) loopCount / finalSum) / loopCount, 0));
                    }
                    loc.add(finalDirection);
                    if (count++ % trailInterval == 0) trail(loc.clone());
                }

                return true;
            }
        };
    }
}
