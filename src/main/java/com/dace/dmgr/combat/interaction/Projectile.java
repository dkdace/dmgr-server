package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * 투사체. 유한한 탄속을 가지는 총알을 관리하는 클래스.
 */
public abstract class Projectile extends Bullet {
    /** 투사체의 속력. (단위: 블록/s) */
    protected final int speed;
    /** 투사체가 유지되는 시간 (tick). -1로 설정 시 무한 지속 */
    protected final long duration;
    /** 중력 작용 여부 */
    protected final boolean hasGravity;
    /** 피해 증가량 */
    @Getter
    private final double damageIncrement;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter 발사자
     * @param speed   투사체의 속력. (단위: 블록/s)
     * @param option  선택적 옵션
     * @see ProjectileOption
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed, @NonNull ProjectileOption option) {
        super(shooter, option.trailInterval, option.startDistance, option.maxDistance, option.size, option.condition);
        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.speed = speed;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter 발사자
     * @param speed   투사체의 속력. (단위: 블록/s)
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed) {
        super(shooter, ProjectileOption.TRAIL_INTERVAL_DEFAULT, ProjectileOption.START_DISTANCE_DEFAULT, ProjectileOption.MAX_DISTANCE_DEFAULT,
                ProjectileOption.SIZE_DEFAULT, ProjectileOption.CONDITION_DEFAULT);
        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.speed = speed;
        this.duration = ProjectileOption.DURATION_DEFAULT;
        this.hasGravity = ProjectileOption.HAS_GRAVITY_DEFAULT;
    }

    /**
     * 투사체 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    @Override
    public final void shoot(@NonNull Location origin, @NonNull Vector direction) {
        velocity = direction.clone().normalize().multiply(HITBOX_INTERVAL);
        location = origin.clone();
        location.add(direction.clone().multiply(startDistance));
        HashSet<Damageable> targets = new HashSet<>();
        int loopCount = (int) (speed / (20.0 / (1.0 / HITBOX_INTERVAL)));
        int sum = IntStream.rangeClosed(0, loopCount).sum();

        TaskUtil.addTask(shooter, new IntervalTask(new Function<Long, Boolean>() {
            int count = 0;

            @Override
            public Boolean apply(Long i) {
                for (int j = 0; j < loopCount; j++) {
                    if ((duration != -1 && i >= duration) || location.distance(origin) > maxDistance)
                        return false;

                    if (!onInterval())
                        return false;

                    if (!LocationUtil.isNonSolid(location) && !handleBlockCollision())
                        return false;

                    if (!findTargetAndHandleCollision(targets))
                        return false;

                    if (hasGravity && LocationUtil.isNonSolid(location.clone().subtract(0, 0.1, 0)))
                        velocity.subtract(new Vector(0, 0.045 * ((double) loopCount / sum) / loopCount, 0));

                    if (velocity.length() > 0.01)
                        location.add(velocity);
                    if (count++ % trailInterval == 0)
                        trail();
                }

                return true;
            }
        }, isCancelled -> onDestroy(), 1));
    }

    @Override
    protected boolean onInterval() {
        return true;
    }
}
