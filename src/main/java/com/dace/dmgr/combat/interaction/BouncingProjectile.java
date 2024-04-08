package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * 튕기는 투사체. 투사체 중 벽이나 엔티티에 튕기는 투사체를 관리하는 클래스.
 */
public abstract class BouncingProjectile extends Projectile {
    /** 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김 */
    protected int bouncing;
    /** 투사체가 튕겼을 때의 속력 배수 */
    protected double bounceVelocityMultiplier;
    /** 바닥에 닿았을 때 제거 여부 */
    protected boolean destroyOnHitFloor;

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link BouncingProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter        발사자
     * @param velocity       투사체의 속력. (단위: 블록/s)
     * @param bouncing       투사체가 튕기는 횟수. {@code -1}로 설정 시 계속 튕김
     * @param option         투사체의 선택적 옵션
     * @param bouncingOption 튕기는 투사체의 선택적 옵션
     * @see ProjectileOption
     * @see BouncingProjectileOption
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int velocity, int bouncing, @NonNull ProjectileOption option,
                                 @NonNull BouncingProjectileOption bouncingOption) {
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
     * @param shooter  발사자
     * @param speed    투사체의 속력. (단위: 블록/s)
     * @param bouncing 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     * @param option   투사체의 선택적 옵션
     * @see ProjectileOption
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, int bouncing, @NonNull ProjectileOption option) {
        super(shooter, speed, option);
        BouncingProjectileOption bouncingOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사자
     * @param speed    투사체의 속력. (단위: 블록/s)
     * @param bouncing 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, int bouncing) {
        super(shooter, speed);
        BouncingProjectileOption bouncingOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    @Override
    protected final boolean onHitBlock(@NonNull Block hitBlock) {
        if (onHitBlockBouncing(hitBlock))
            return true;

        if (bouncing == -1 || bouncing-- > 0)
            return handleBounce();

        return false;
    }

    /**
     * 총알이 블록에 맞았을 때 실행할 작업.
     *
     * @param hitBlock 맞은 블록
     * @return 관통 여부. {@code true} 반환 시 블록 관통, {@code false} 반환 시 도탄됨
     */
    protected abstract boolean onHitBlockBouncing(@NonNull Block hitBlock);

    @Override
    protected final boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
        if (onHitEntityBouncing(target, isCrit))
            return true;

        if (bouncing == -1 || bouncing-- > 0) {
            velocity.multiply(-bounceVelocityMultiplier * 0.5);
            return true;
        }

        return false;
    }

    /**
     * 총알이 엔티티에 맞았을 때 실행할 작업.
     *
     * @param target 맞은 엔티티
     * @param isCrit 치명타 여부
     * @return 관통 여부. {@code true} 반환 시 엔티티 관통, {@code false} 반환 시 도탄됨
     */
    protected abstract boolean onHitEntityBouncing(@NonNull Damageable target, boolean isCrit);

    /**
     * 투사체의 도탄 로직을 처리한다.
     */
    private boolean handleBounce() {
        Location beforeHitBlockLocation = location.getBlock().getLocation();
        Location hitBlockLocation = location.clone().add(velocity.clone().normalize().multiply(0.5)).getBlock().getLocation();
        Vector hitDir = hitBlockLocation.subtract(beforeHitBlockLocation).toVector();
        if (destroyOnHitFloor && !LocationUtil.isNonSolid(beforeHitBlockLocation.subtract(0, 0.1, 0)))
            return false;

        velocity.multiply(bounceVelocityMultiplier);
        if (Math.abs(hitDir.getX()) > 0.5)
            velocity.setX(-velocity.getX());
        else if (Math.abs(hitDir.getY()) > 0.5)
            velocity.setY(-velocity.getY());
        else if (Math.abs(hitDir.getZ()) > 0.5)
            velocity.setZ(-velocity.getZ());

        return true;
    }
}
