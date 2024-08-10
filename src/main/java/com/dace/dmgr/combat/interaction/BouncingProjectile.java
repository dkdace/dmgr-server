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
    /** 투사체가 튕겼을 때의 속력 배수 */
    protected final double bounceVelocityMultiplier;
    /** 바닥에 닿았을 때 제거 여부 */
    protected final boolean destroyOnHitFloor;
    /** 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김 */
    protected int bouncing;

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * <p>튕기는 투사체의 선택적 옵션은 {@link BouncingProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter        발사자
     * @param velocity       투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param bouncing       투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     * @param option         투사체의 선택적 옵션
     * @param bouncingOption 튕기는 투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see ProjectileOption
     * @see BouncingProjectileOption
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int velocity, int bouncing, @NonNull ProjectileOption option,
                                 @NonNull BouncingProjectileOption bouncingOption) {
        super(shooter, velocity, option);
        validateArgs(bouncing);

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
     * @param speed    투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param bouncing 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     * @param option   투사체의 선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     * @see ProjectileOption
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, int bouncing, @NonNull ProjectileOption option) {
        super(shooter, speed, option);
        validateArgs(bouncing);

        BouncingProjectileOption bouncingOption = BouncingProjectileOption.builder().build();
        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = bouncingOption.bounceVelocityMultiplier;
        this.destroyOnHitFloor = bouncingOption.destroyOnHitFloor;
    }

    /**
     * 튕기는 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사자
     * @param speed    투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param bouncing 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected BouncingProjectile(@NonNull CombatEntity shooter, int speed, int bouncing) {
        super(shooter, speed);
        validateArgs(bouncing);

        this.bouncing = bouncing;
        this.bounceVelocityMultiplier = BouncingProjectileOption.BOUNCE_VELOCITY_MULTIPLIER_DEFAULT;
        this.destroyOnHitFloor = BouncingProjectileOption.DESTROY_ON_HIT_FLOOR_DEFAULT;
    }

    /**
     * 인자값이 유효하지 않으면 예외를 발생시킨다.
     *
     * @param bouncing 투사체가 튕기는 횟수. -1로 설정 시 계속 튕김
     */
    private static void validateArgs(int bouncing) {
        if (bouncing < -1)
            throw new IllegalArgumentException("'bouncing'이 -1 이상이어야 함");
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
            getVelocity().multiply(-bounceVelocityMultiplier * 0.5);
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
        Location beforeHitBlockLocation = getLocation().getBlock().getLocation();
        Location hitBlockLocation = getLocation().clone().add(getVelocity().clone().normalize().multiply(0.5)).getBlock().getLocation();
        Vector hitDir = hitBlockLocation.subtract(beforeHitBlockLocation).toVector();
        if (destroyOnHitFloor && !LocationUtil.isNonSolid(beforeHitBlockLocation.subtract(0, 0.1, 0)))
            return false;

        getVelocity().multiply(bounceVelocityMultiplier);
        if (Math.abs(hitDir.getX()) > 0.5)
            getVelocity().setX(-getVelocity().getX());
        else if (Math.abs(hitDir.getY()) > 0.5)
            getVelocity().setY(-getVelocity().getY());
        else if (Math.abs(hitDir.getZ()) > 0.5)
            getVelocity().setZ(-getVelocity().getZ());

        return true;
    }
}
