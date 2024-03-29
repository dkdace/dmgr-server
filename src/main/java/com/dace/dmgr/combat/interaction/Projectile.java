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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * 투사체. 유한한 탄속을 가지는 총알을 관리하는 클래스.
 */
public abstract class Projectile extends Bullet {
    /** 피해 증가량 */
    @Getter
    private final double damageIncrement;
    /** 투사체의 속력. (단위: 블록/s) */
    protected int velocity;
    /** 투사체가 유지되는 시간 (tick). {@code -1}로 설정 시 무한 지속 */
    protected long duration;
    /** 중력 작용 여부 */
    protected boolean hasGravity;
    /** 지면 고정 여부 */
    protected boolean isOnGround;

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter  발사자
     * @param velocity 투사체의 속력. (단위: 블록/s)
     * @param option   선택적 옵션
     * @throws IllegalArgumentException 투사체 옵션의 'hasGravity'와 'isOnGround'가 동시에 true이면 발생
     * @see ProjectileOption
     */
    protected Projectile(@NonNull CombatEntity shooter, int velocity, @NonNull ProjectileOption option) {
        super(shooter, option.trailInterval, option.startDistance, option.maxDistance, option.size, option.condition);
        if (option.hasGravity && option.isOnGround)
            throw new IllegalArgumentException("투사체 옵션의 'hasGravity'와 'isOnGround'는 동시에 true일 수 없음");

        this.damageIncrement = (shooter instanceof Attacker) ? ((Attacker) shooter).getAttackModule().getDamageMultiplierStatus().getValue() : 1;
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
        this.isOnGround = option.isOnGround;
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
        this.size = option.size;
        this.condition = option.condition;
        this.velocity = velocity;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
        this.isOnGround = option.isOnGround;
    }

    /**
     * 투사체 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    @Override
    public final void shoot(@NonNull Location origin, @NonNull Vector direction) {
        direction.normalize().multiply(HITBOX_INTERVAL);
        Location loc = origin.clone();
        loc.add(direction.clone().multiply(startDistance));
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

                    if (isOnGround && !handleGround(loc))
                        return false;

                    if (!LocationUtil.isNonSolid(loc) && !Projectile.this.handleBlockCollision(loc, finalDirection))
                        return false;

                    if (!Projectile.this.findTargetAndHandleCollision(loc, finalDirection, targets, condition))
                        return false;

                    if (hasGravity && LocationUtil.isNonSolid(loc.clone().subtract(0, 0.1, 0)))
                        finalDirection.subtract(new Vector(0, 0.045 * ((double) loopCount / finalSum) / loopCount, 0));

                    if (finalDirection.length() > 0.01)
                        loc.add(finalDirection);
                    if (count++ % trailInterval == 0)
                        Projectile.this.trail(loc.clone(), finalDirection.clone().normalize());
                }

                return true;
            }
        }, isCancelled -> onDestroy(loc.clone()), 1));
    }

    /**
     * 투사체의 지면 고정 로직을 처리한다.
     *
     * @param location 위치
     */
    private boolean handleGround(@NonNull Location location) {
        if (!LocationUtil.isNonSolid(location)) {
            Location shiftLocUp = location.clone();
            for (int k = 1; k <= 16; k++) {
                if (!LocationUtil.isNonSolid(shiftLocUp.add(0, HITBOX_INTERVAL, 0)))
                    continue;

                location.add(0, k * HITBOX_INTERVAL, 0);
                return true;
            }

            return false;
        } else if (LocationUtil.isNonSolid(location.clone().subtract(0, HITBOX_INTERVAL, 0))) {
            Location shiftLocDown = location.clone();
            for (int k = 1; k <= 16; k++) {
                if (LocationUtil.isNonSolid(shiftLocDown.subtract(0, HITBOX_INTERVAL, 0)))
                    continue;

                location.subtract(0, k * HITBOX_INTERVAL - HITBOX_INTERVAL, 0);
                return true;
            }

            return false;
        }

        return true;
    }
}
