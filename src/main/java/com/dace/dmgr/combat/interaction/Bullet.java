package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Predicate;

/**
 * 총알. 원거리 판정(투사체, 히트스캔) 등을 관리하기 위한 클래스.
 *
 * @see Hitscan
 * @see Projectile
 */
public abstract class Bullet {
    /** 궤적 상 히트박스 판정점 간 거리 기본값. (단위: 블록) */
    protected static final double HITBOX_INTERVAL = 0.125;
    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 트레일 이벤트 ({@link Bullet#trail()})를 호출하는 주기. (단위: 판정점 개수) */
    protected final int trailInterval;
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    protected final double startDistance;
    /** 총알의 최대 사거리. (단위: 블록) */
    protected final double maxDistance;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    protected final double size;
    /** 대상 엔티티를 찾는 조건 */
    protected final Predicate<CombatEntity> condition;
    /** 투사체의 현재 위치 */
    protected Location location;
    /** 투사체의 현재 속도 */
    protected Vector velocity;

    /**
     * 총알 인스턴스를 생성한다.
     *
     * @param shooter       발사자 엔티티
     * @param trailInterval 트레일 이벤트 ({@link Bullet#trail()})를 호출하는 주기. (단위: 판정점 개수)
     * @param startDistance 발사 위치로부터 총알이 생성되는 거리. (단위: 블록)
     * @param maxDistance   총알의 최대 사거리. (단위: 블록)
     * @param size          총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록)
     * @param condition     대상 엔티티를 찾는 조건
     * @see HitscanOption
     */
    protected Bullet(@NonNull CombatEntity shooter, int trailInterval, double startDistance, double maxDistance, double size, @NonNull Predicate<CombatEntity> condition) {
        this.shooter = shooter;
        this.trailInterval = trailInterval;
        this.startDistance = startDistance;
        this.maxDistance = maxDistance;
        this.size = size;
        this.condition = condition;
    }

    /**
     * 투사체의 현재 위치를 반환한다.
     *
     * @return 현재 위치
     */
    @Nullable
    public final Location getLocation() {
        return location == null ? null : location.clone();
    }

    /**
     * 투사체의 현재 속도를 반환한다.
     *
     * @return 현재 속도
     */
    @Nullable
    public final Vector getVelocity() {
        return velocity == null ? null : velocity.clone();
    }

    /**
     * 지정한 위치와 방향으로 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    public abstract void shoot(@NonNull Location origin, @NonNull Vector direction);

    /**
     * 지정한 위치에서 엔티티가 보는 방향으로 총알을 발사한다.
     *
     * @param origin 발사 위치
     */
    public final void shoot(@NonNull Location origin) {
        shoot(origin, shooter.getEntity().getLocation().getDirection());
    }

    /**
     * 엔티티에서 지정한 방향으로, 엔티티의 눈 위치에서 총알을 발사한다.
     */
    public final void shoot(@NonNull Vector direction) {
        if (shooter.getEntity() instanceof LivingEntity)
            shoot(((LivingEntity) shooter.getEntity()).getEyeLocation(), direction);
        else
            shoot(shooter.getEntity().getLocation(), direction);
    }

    /**
     * 엔티티에서 엔티티가 보는 방향으로, 엔티티의 눈 위치에서 총알을 발사한다.
     */
    public final void shoot() {
        if (shooter.getEntity() instanceof LivingEntity)
            shoot(((LivingEntity) shooter.getEntity()).getEyeLocation());
        else
            shoot(shooter.getEntity().getLocation());
    }

    /**
     * 총알의 블록 충돌 로직을 처리한다.
     *
     * @return {@link Bullet#onHitBlock(Block)}의 반환값
     */
    protected final boolean handleBlockCollision() {
        Block hitBlock = location.getBlock();

        if (velocity.length() > 0.01)
            while (!LocationUtil.isNonSolid(location))
                location.subtract(velocity);

        onHit();
        return onHitBlock(hitBlock);
    }

    /**
     * 총알 주변의 지정한 조건을 만족하는 엔티티를 찾고 피격 로직을 처리한다.
     *
     * @param targets   피격자 목록
     * @param condition 대상 엔티티를 찾는 조건
     * @return {@link Bullet#onHitEntity(Damageable, boolean)}의 반환값
     */
    protected final boolean findTargetAndHandleCollision(@NonNull HashSet<Damageable> targets, @NonNull Predicate<CombatEntity> condition) {
        Damageable target = (Damageable) CombatUtil.getNearCombatEntity(shooter.getGame(), location, size,
                condition.and(Damageable.class::isInstance));

        if (target != null && targets.add(target)) {
            boolean isCrit = false;

            onHit();
            if (target instanceof HasCritHitbox)
                isCrit = ((HasCritHitbox) target).getCritHitbox().isInHitbox(location, size);
            return onHitEntity(target, isCrit);
        }

        return true;
    }

    /**
     * 트레일 주기 ({@link Bullet#trailInterval})마다 실행될 작업.
     *
     * <p>주로 파티클을 남길 때 사용한다.</p>
     */
    protected void trail() {
        // 미사용
    }

    /**
     * 매 판정점마다 실행할 작업.
     *
     * @return 소멸 여부. {@code false} 반환 시 소멸
     */
    protected abstract boolean onInterval();

    /**
     * 총알이 블록에 맞았을 때 실행될 작업.
     *
     * @param hitBlock 맞은 블록
     * @return 관통 여부. {@code true} 반환 시 블록 관통
     * @see Bullet#onHit()
     * @see Bullet#onHitEntity(Damageable, boolean)
     */
    protected abstract boolean onHitBlock(@NonNull Block hitBlock);

    /**
     * 총알이 엔티티에 맞았을 때 실행될 작업.
     *
     * @param target 맞은 엔티티
     * @param isCrit 치명타 여부
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통
     * @see Bullet#onHit()
     * @see Bullet#onHitBlock(Block)
     */
    protected abstract boolean onHitEntity(@NonNull Damageable target, boolean isCrit);

    /**
     * 총알이 어느 곳이든(블록 또는 엔티티) 맞았을 때 실행될 작업.
     *
     * @see Bullet#onHitBlock(Block)
     * @see Bullet#onHitEntity(Damageable, boolean)
     */
    protected void onHit() {
        // 미사용
    }

    /**
     * 총알이 소멸했을 때 실행될 작업.
     */
    protected void onDestroy() {
        // 미사용
    }
}
