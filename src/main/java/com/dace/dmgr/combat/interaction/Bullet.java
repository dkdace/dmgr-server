package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
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
    protected static final double HITBOX_INTERVAL = 1 / 8.0;

    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 트레일 이벤트 ({@link Bullet#onTrailInterval()})를 호출하는 주기. (단위: 판정점 개수) */
    protected final int trailInterval;
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    protected final double startDistance;
    /** 총알의 최대 사거리. (단위: 블록) */
    protected final double maxDistance;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    protected final double size;
    /** 대상 엔티티를 찾는 조건 */
    protected final Predicate<CombatEntity> condition;
    /** 피격자 목록 */
    private final HashSet<Damageable> targets = new HashSet<>();
    /** 총알의 현재 위치 */
    @Nullable
    private Location location;
    /** 총알의 현재 속도 */
    @Nullable
    private Vector velocity;
    /** 블록 피격 여부 */
    private boolean isInBlock = false;

    /**
     * 총알 인스턴스를 생성한다.
     *
     * @param shooter       발사자 엔티티
     * @param trailInterval 트레일 이벤트 ({@link Bullet#onTrailInterval()})를 호출하는 주기. (단위: 판정점 개수). 0 이상의 값
     * @param startDistance 발사 위치로부터 총알이 생성되는 거리. (단위: 블록). 0 이상의 값
     * @param maxDistance   총알의 최대 사거리. (단위: 블록). 0 이상의 값
     * @param size          총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록). 0 이상의 값
     * @param condition     대상 엔티티를 찾는 조건
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see HitscanOption
     */
    protected Bullet(@NonNull CombatEntity shooter, int trailInterval, double startDistance, double maxDistance, double size,
                     @NonNull Predicate<@NonNull CombatEntity> condition) {
        if (trailInterval < 0 || startDistance < 0 || maxDistance < 0 || size < 0)
            throw new IllegalArgumentException("'trailInterval', 'startDistance', 'maxDistance' 및 'size'가 0 이상이어야 함");

        this.shooter = shooter;
        this.trailInterval = trailInterval;
        this.startDistance = startDistance;
        this.maxDistance = maxDistance;
        this.size = size;
        this.condition = condition;
    }

    /**
     * 총알의 현재 위치를 반환한다.
     *
     * @return 현재 위치
     * @apiNote 실제 위치 객체를 반환한다. 즉 값에 대한 변경이 실제
     * 총알의 위치에 영향을 미침
     */
    @NonNull
    public final Location getLocation() {
        return Validate.notNull(location);
    }

    /**
     * 총알의 현재 속도를 반환한다.
     *
     * @return 현재 속도
     * @apiNote 실제 속도 객체를 반환한다. 즉 값에 대한 변경이 실제
     * 총알의 속도에 영향을 미침
     */
    @NonNull
    public final Vector getVelocity() {
        return Validate.notNull(velocity);
    }

    /**
     * 지정한 위치와 방향으로 총알을 발사한다.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     */
    public final void shoot(@NonNull Location origin, @NonNull Vector direction) {
        velocity = direction.clone().normalize().multiply(HITBOX_INTERVAL);
        location = origin.clone();
        location.add(direction.clone().multiply(startDistance));

        onShoot(origin, direction);
    }

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
     * 총알이 발사됐을 때 ({@link Bullet#shoot(Location, Vector)} 호출 시) 실행될 작업.
     *
     * @param origin    발사 위치
     * @param direction 발사 방향
     * @see Bullet#shoot(Location, Vector)
     */
    protected abstract void onShoot(@NonNull Location origin, @NonNull Vector direction);

    /**
     * 매 판정점마다 실행할 작업.
     *
     * @return 소멸 여부. {@code false} 반환 시 소멸
     * @see Bullet#handleBlockCollision()
     * @see Bullet#findTargetAndHandleCollision()
     */
    @MustBeInvokedByOverriders
    protected boolean onInterval() {
        return (LocationUtil.isNonSolid(getLocation()) || handleBlockCollision()) && findTargetAndHandleCollision();
    }

    /**
     * 총알의 블록 충돌 로직을 처리한다.
     *
     * @return {@link Bullet#onHitBlock(Block)}의 반환값
     */
    private boolean handleBlockCollision() {
        Block hitBlock = getLocation().getBlock();
        Location hitLocation = getLocation().clone();

        if (!isInBlock && getVelocity().length() > 0.01)
            while (!LocationUtil.isNonSolid(getLocation()))
                getLocation().subtract(getVelocity());

        onHit();
        if (onHitBlock(hitBlock)) {
            isInBlock = !LocationUtil.isNonSolid(getLocation().clone().add(getVelocity()));
            if (isInBlock)
                location = hitLocation;

            return true;
        }

        return false;
    }

    /**
     * 총알 주변의 지정한 조건을 만족하는 엔티티를 찾고 피격 로직을 처리한다.
     *
     * @return {@link Bullet#onHitEntity(Damageable, boolean)}의 반환값
     */
    private boolean findTargetAndHandleCollision() {
        Damageable target = (Damageable) CombatUtil.getNearCombatEntity(shooter.getGame(), getLocation(), size, condition.and(Damageable.class::isInstance));

        if (target != null && targets.add(target)) {
            onHit();

            boolean isCrit = target instanceof HasCritHitbox && ((HasCritHitbox) target).getCritHitbox().isInHitbox(getLocation(), size);
            return onHitEntity(target, isCrit);
        }

        return true;
    }

    /**
     * 트레일 주기 ({@link Bullet#trailInterval})마다 실행될 작업.
     *
     * <p>주로 파티클을 남길 때 사용한다.</p>
     */
    protected void onTrailInterval() {
        // 미사용
    }

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
