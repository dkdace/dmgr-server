package com.dace.dmgr.combat;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * 튕기는 투사체. 투사체 중 벽이나 엔티티에 튕기는 투사체를 관리하는 클래스.
 */
public abstract class BouncingProjectile extends Projectile {
    /** 투사체가 튕기는 횟수 */
    protected int bouncing;
    /** 투사체가 튕겼을 때의 속력 계수 */
    protected float bounceVelocityMultiplier;
    /** 바닥에 닿았을 때 제거 여부 */
    protected boolean destroyOnHitFloor;

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
    protected BouncingProjectile(CombatEntity<?> shooter, int velocity, int bouncing, ProjectileOption option, BouncingProjectileOption secondOption) {
        super(shooter, velocity, option);
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = secondOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = secondOption.destroyOnHitFloor;
    }

    /**
     * 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 속력
     */
    protected BouncingProjectile(CombatEntity<?> shooter, int velocity, int bouncing) {
        super(shooter, velocity);
        ProjectileOption option = ProjectileOption.builder().build();
        this.trailInterval = option.trailInterval;
        this.maxDistance = option.maxDistance;
        this.penetrating = option.penetrating;
        this.hitboxMultiplier = option.hitboxMultiplier;
        this.hasGravity = option.hasGravity;
        BouncingProjectileOption secondOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = secondOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = secondOption.destroyOnHitFloor;
    }

    @Override
    public final boolean onHitBlock(Location location, Vector direction, Block hitBlock) {
        super.onHitBlock(location, direction, hitBlock);
        if (onHitBlockBouncing(location, direction, hitBlock))
            return true;

        if (bouncing-- > 0)
            return handleBounce(location.clone(), direction);

        return false;
    }

    public boolean onHitBlockBouncing(Location location, Vector direction, Block hitBlock) {
        return false;
    }

    @Override
    public final boolean onHitEntity(Location location, Vector direction, CombatEntity<?> target, boolean isCrit) {
        if (onHitEntityBouncing(location, direction, target, isCrit))
            return true;

        if (bouncing-- > 0) {
            direction.multiply(-bounceVelocityMultiplier);
            return true;
        }

        return false;
    }

    public abstract boolean onHitEntityBouncing(Location location, Vector direction, CombatEntity<?> target, boolean isCrit);

    /**
     * 투사체의 도탄 로직을 처리한다.
     *
     * @param location  위치
     * @param direction 발사 방향
     */
    private boolean handleBounce(Location location, Vector direction) {
        Location hitBlockLocation = location.getBlock().getLocation();
        Location beforeHitBlockLocation = location.subtract(direction).getBlock().getLocation();
        Vector hitDir = hitBlockLocation.subtract(beforeHitBlockLocation).toVector();
        if (destroyOnHitFloor && !LocationUtil.isNonSolid(beforeHitBlockLocation.subtract(0, 0.1, 0)))
            return false;

        direction.multiply(bounceVelocityMultiplier);
        if (Math.abs(hitDir.getX()) > 0.5)
            direction.setX(-direction.getX());
        else if (Math.abs(hitDir.getY()) > 0.5)
            direction.setY(-direction.getY());
        else if (Math.abs(hitDir.getZ()) > 0.5)
            direction.setZ(-direction.getZ());

        return true;
    }
}