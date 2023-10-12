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
    /** 투사체가 튕기는 횟수. {@code -1}로 설정 시 계속 튕김 */
    protected int bouncing;
    /** 투사체가 튕겼을 때의 속력 계수 */
    protected float bounceVelocityMultiplier;
    /** 바닥에 닿았을 때 제거 여부 */
    protected boolean destroyOnHitFloor;

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link BouncingProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter        발사하는 엔티티
     * @param velocity       투사체의 속력. 단위: 블록/s
     * @param bouncing       투사체가 튕기는 횟수. {@code -1}로 설정 시 계속 튕김
     * @param option         투사체의 선택적 옵션
     * @param bouncingOption 튕기는 투사체의 선택적 옵션
     * @see BouncingProjectileOption
     */
    protected BouncingProjectile(CombatEntity<?> shooter, int velocity, int bouncing, ProjectileOption option, BouncingProjectileOption bouncingOption) {
        super(shooter, velocity, option);
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 투사체의 속력. 단위: 블록/s
     * @param bouncing 투사체가 튕기는 횟수. {@code -1}로 설정 시 계속 튕김
     * @param option   투사체의 선택적 옵션
     * @see ProjectileOption
     */
    protected BouncingProjectile(CombatEntity<?> shooter, int velocity, int bouncing, ProjectileOption option) {
        super(shooter, velocity, option);
        BouncingProjectileOption bouncingOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사하는 엔티티
     * @param velocity 투사체의 속력. 단위: 블록/s
     * @param bouncing 투사체가 튕기는 횟수. {@code -1}로 설정 시 계속 튕김
     */
    protected BouncingProjectile(CombatEntity<?> shooter, int velocity, int bouncing) {
        super(shooter, velocity);
        ProjectileOption option = ProjectileOption.builder().build();
        this.trailInterval = option.trailInterval;
        this.maxDistance = option.maxDistance;
        this.penetrating = option.penetrating;
        this.hitboxMultiplier = option.hitboxMultiplier;
        this.duration = option.duration;
        this.hasGravity = option.hasGravity;
        BouncingProjectileOption bouncingOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    @Override
    public final boolean onHitBlock(Location location, Vector direction, Block hitBlock) {
        if (onHitBlockBouncing(location, direction, hitBlock))
            return true;

        if (bouncing == -1 || bouncing-- > 0)
            return handleBounce(location.clone(), direction);

        return false;
    }

    /**
     * 총알이 블록에 맞았을 때 실행할 작업.
     *
     * @param location  맞은 위치
     * @param direction 발사 방향
     * @param hitBlock  맞은 블록
     * @return 관통 여부. {@code true} 반환 시 블록 관통, {@code false} 반환 시 도탄됨
     */
    public abstract boolean onHitBlockBouncing(Location location, Vector direction, Block hitBlock);

    @Override
    public final boolean onHitEntity(Location location, Vector direction, CombatEntity<?> target, boolean isCrit) {
        if (onHitEntityBouncing(location, direction, target, isCrit))
            return true;

        if (bouncing == -1 || bouncing-- > 0) {
            direction.multiply(-bounceVelocityMultiplier);
            return true;
        }

        return false;
    }

    /**
     * 총알이 블록에 맞았을 때 실행할 작업.
     *
     * @param location  맞은 위치
     * @param direction 발사 방향
     * @param target    맞은 엔티티
     * @param isCrit    치명타 여부
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통, {@code false} 반환 시 도탄됨
     */
    public abstract boolean onHitEntityBouncing(Location location, Vector direction, CombatEntity<?> target, boolean isCrit);

    /**
     * 투사체의 도탄 로직을 처리한다.
     *
     * @param location  위치
     * @param direction 발사 방향
     */
    private boolean handleBounce(Location location, Vector direction) {
        Location beforeHitBlockLocation = location.getBlock().getLocation();
        Location hitBlockLocation = location.add(direction.clone().multiply(0.5)).getBlock().getLocation();
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
