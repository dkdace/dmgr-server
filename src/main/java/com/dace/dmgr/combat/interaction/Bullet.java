package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.function.Predicate;

/**
 * 총알. 원거리 공격(투사체, 히트스캔) 등을 관리하기 위한 클래스.
 *
 * @see Hitscan
 * @see Projectile
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
abstract class Bullet {
    /** 발사 위치로부터 총알이 생성되는 거리. (단위: 블록) */
    protected static final double START_DISTANCE = 0.5;
    /** 궤적 상 히트박스 판정점 간 거리 기본값. (단위: 블록) */
    protected static final double HITBOX_INTERVAL = 0.125;
    /** 발사자 엔티티 */
    @NonNull
    @Getter
    protected final CombatEntity shooter;
    /** 트레일 이벤트 ({@link Bullet#trail(Location, Vector)})를 호출하는 주기. (단위: 판정점 개수) */
    protected int trailInterval;
    /** 총알의 최대 사거리. (단위: 블록) */
    protected double maxDistance;
    /** 총알의 판정 크기. 판정의 엄격함에 영향을 미침. (단위: 블록) */
    protected double size;
    /** 대상 엔티티를 찾는 조건 */
    protected Predicate<CombatEntity> condition;

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
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation(), direction);
        else
            shoot(shooter.getEntity().getLocation(), direction);
    }

    /**
     * 엔티티에서 엔티티가 보는 방향으로, 엔티티의 눈 위치에서 총알을 발사한다.
     */
    public final void shoot() {
        if (shooter instanceof CombatUser)
            shoot(((CombatUser) shooter).getEntity().getEyeLocation());
        else
            shoot(shooter.getEntity().getLocation());
    }

    /**
     * 총알의 블록 충돌 로직을 처리한다.
     *
     * @param location 맞은 위치
     * @param velocity 발사 속도
     * @return {@link Bullet#onHitBlock(Location, Vector, Block)}의 반환값
     */
    protected final boolean handleBlockCollision(@NonNull Location location, @NonNull Vector velocity) {
        Vector subDir = velocity.clone().multiply(0.5);
        Block hitBlock = location.getBlock();

        if (velocity.length() > 0.01)
            while (!LocationUtil.isNonSolid(location))
                location.subtract(subDir);

        onHit(location.clone());
        return onHitBlock(location.clone(), velocity, hitBlock);
    }

    /**
     * 총알 주변의 지정한 조건을 만족하는 엔티티를 찾고 피격 로직을 처리한다.
     *
     * @param location  맞은 위치
     * @param velocity  발사 속도
     * @param targets   피격자 목록
     * @param condition 대상 엔티티를 찾는 조건
     * @return {@link Bullet#onHitEntity(Location, Vector, Damageable, boolean)}의 반환값
     */
    protected final boolean findTargetAndHandleCollision(@NonNull Location location, @NonNull Vector velocity, @NonNull Set<CombatEntity> targets,
                                                         @NonNull Predicate<CombatEntity> condition) {
        Damageable target = (Damageable) CombatUtil.getNearCombatEntity(shooter.getGame(), location.clone(), size,
                condition.and(Damageable.class::isInstance));
        boolean isCrit = false;

        if (target != null && targets.add(target)) {
            onHit(location.clone());
            if (target instanceof HasCritHitbox)
                isCrit = ((HasCritHitbox) target).getCritHitbox().isInHitbox(location, size);
            return onHitEntity(location.clone(), velocity, target, isCrit);
        }

        return true;
    }

    /**
     * 트레일 주기 ({@link Bullet#trailInterval})마다 실행될 작업.
     *
     * <p>주로 파티클을 남길 때 사용한다.</p>
     *
     * @param location  위치
     * @param direction 발사 방향
     */
    protected void trail(@NonNull Location location, @NonNull Vector direction) {
        // 미사용
    }

    /**
     * 총알이 블록에 맞았을 때 실행될 작업.
     *
     * @param location 맞은 위치
     * @param velocity 발사 속도
     * @param hitBlock 맞은 블록
     * @return 관통 여부. {@code true} 반환 시 블록 관통
     * @see Bullet#onHit
     * @see Bullet#onHitEntity
     */
    protected abstract boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock);

    /**
     * 총알이 엔티티에 맞았을 때 실행될 작업.
     *
     * @param location 맞은 위치
     * @param velocity 발사 속도
     * @param target   맞은 엔티티
     * @param isCrit   치명타 여부
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통
     * @see Bullet#onHit
     * @see Bullet#onHitBlock
     */
    protected abstract boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit);

    /**
     * 총알이 어느 곳이든(블록 또는 엔티티) 맞았을 때 실행될 작업.
     *
     * @param location 맞은 위치
     * @see Bullet#onHitBlock
     * @see Bullet#onHitEntity
     */
    protected void onHit(@NonNull Location location) {
    }

    /**
     * 총알이 소멸했을 때 실행될 작업.
     *
     * @param location 소멸한 위치
     */
    protected void onDestroy(@NonNull Location location) {
    }
}
