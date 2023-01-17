package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.ICombatEntity;
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
    /** 중력의 영향을 받는지 여부 */
    protected boolean hasGravity;
    /** 투사체가 튕기는 지 여부 */
    protected boolean bouncing;

    /**
     * 투사체 인스턴스를 생성한다.<br>
     *
     * 투사체를 생성에 필요한 옵션이 가변적이므로 매개변수 대신 {@link ProjectileOption} 객체를 통해 전달받는다.
     * {@link ProjectileOption} 객체는 {@link ProjectileOption.Builder}를 통해 제작할 수 있다.
     * 다음 예시를 참고하라: <pre>
     * ProjectileOption option = new ProjectileOption.Builder(combatUser, VELOCITY)
     *                              .trailInterval(5)
     *                              .build();
     * new Projectile(option){ /* ... &#42;/}.shoot();
     * </pre>
     * @param option 투사체 옵션
     * @see ProjectileOption
     * @deprecated
     */
    public Projectile(ProjectileOption option) {
        super(option.shooter, option.penetrating, option.trailInterval, option.hitboxMultiplier);
        this.velocity = option.velocity;
        this.hasGravity = option.hasGravity;
        this.bouncing = option.bouncing;
    }



    /**
     * 투사체를 발사한다.
     * @param origin    발화점
     * @param direction 발사 방향
     * @param spread    탄퍼짐 정도. 단위: ×0.02블록/블록
     */
    @Override
    public void shoot(Location origin, Vector direction, float spread) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        direction = VectorUtil.getSpreadedVector(direction, spread);
        Set<ICombatEntity> targetList = new HashSet<>();

        Vector finalDirection = direction;

        new TaskTimer(1) {
            int count = 0;

            @Override
            public boolean run(int _i) {
                for (int i = 0; i < velocity / 5; i++) {
                    if (loc.distance(origin) >= MAX_RANGE)
                        return false;

                    Location hitLoc = loc.clone().add(finalDirection);
                    if (!LocationUtil.isNonSolid(hitLoc)) {
                        Vector subDir = finalDirection.clone().multiply(0.5);

                        while (LocationUtil.isNonSolid(loc))
                            loc.add(subDir);

                        loc.subtract(subDir);
                        onHit(loc);
                        onHitBlock(loc, hitLoc.getBlock());
                        return false;
                    }

                    if (loc.distance(origin) > 0.5) {
                        Map.Entry<ICombatEntity, Boolean> targetEntry
                                = Combat.getNearEnemy(shooter, loc, SIZE * hitboxMultiplier);

                        ICombatEntity target = targetEntry.getKey();
                        if (target != null) {
                            boolean isCrit = targetEntry.getValue();

                            if (targetList.add(target)) {
                                onHit(hitLoc);
                                onHitEntity(hitLoc, target, isCrit);

                                if (!penetrating)
                                    return false;
                            }
                        }
                    }
                    loc.add(finalDirection);
                    if (count++ % trailInterval == 0) trail(loc.clone());
                }

                return true;
            }
        };
    }
}
