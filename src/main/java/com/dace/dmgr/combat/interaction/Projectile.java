package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

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

    /** 반복 횟수 */
    private int loopCount = 0;
    /** 0부터 {@link Projectile#loopCount}까지의 합계 */
    private int sum = 0;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter 발사자
     * @param speed   투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param option  선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see ProjectileOption
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed, @NonNull ProjectileOption option) {
        super(shooter, option.trailInterval, option.startDistance, option.maxDistance, option.size, option.condition);
        if (speed < 0)
            throw new IllegalArgumentException("'speed'가 0 이상이어야 함");
        if (option.duration < -1)
            throw new IllegalArgumentException("ProjectileOption의 'duration'이 -1 이상이어야 함");

        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.speed = speed;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter 발사자
     * @param speed   투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected Projectile(@NonNull CombatEntity shooter, int speed) {
        super(shooter, ProjectileOption.TRAIL_INTERVAL_DEFAULT, ProjectileOption.START_DISTANCE_DEFAULT, ProjectileOption.MAX_DISTANCE_DEFAULT,
                ProjectileOption.SIZE_DEFAULT, ProjectileOption.CONDITION_DEFAULT);
        if (speed < 0)
            throw new IllegalArgumentException("'speed'가 0 이상이어야 함");

        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.speed = speed;
        this.duration = ProjectileOption.DURATION_DEFAULT;
        this.hasGravity = ProjectileOption.HAS_GRAVITY_DEFAULT;
    }

    @Override
    protected final void onShoot(@NonNull Location origin, @NonNull Vector direction) {
        loopCount = (int) (speed / (20.0 / (1.0 / HITBOX_INTERVAL)));
        sum = IntStream.rangeClosed(0, loopCount).sum();

        TaskUtil.addTask(shooter, new IntervalTask(new Function<Long, Boolean>() {
            int count = 0;

            @Override
            public Boolean apply(Long i) {
                for (int j = 0; j < loopCount; j++) {
                    if (!onInterval())
                        return false;

                    if (getVelocity().length() > 0.01)
                        getLocation().add(getVelocity());
                    if (count++ % trailInterval == 0)
                        onTrailInterval();
                }

                return (duration == -1 || i < duration) && getLocation().distance(origin) < maxDistance;
            }
        }, isCancelled -> onDestroy(), 1));
    }

    @Override
    @MustBeInvokedByOverriders
    protected boolean onInterval() {
        if (!super.onInterval())
            return false;

        if (hasGravity && LocationUtil.isNonSolid(getLocation().clone().subtract(0, 0.1, 0)))
            getVelocity().subtract(new Vector(0, 0.045 * ((double) loopCount / sum) / loopCount, 0));

        return true;
    }
}
