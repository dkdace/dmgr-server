package com.dace.dmgr.combat.interaction;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 지면 고정 투사체. 투사체 중 충격파 등 지면에 고정된 투사체를 관리하는 클래스.
 */
public abstract class GroundProjectile extends Projectile {
    /**
     * 지면 고정 투사체 인스턴스를 생성한다.
     *
     * <p>투사체의 선택적 옵션은 {@link ProjectileOption} 객체를 통해 전달받는다.</p>
     *
     * @param shooter  발사자
     * @param velocity 투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @param option   선택적 옵션
     * @throws IllegalArgumentException 인자값이 유효하지 않거나 {@code option}의 {@code hasGravity}가
     *                                  {@code true}이면 발생
     * @see ProjectileOption
     */
    protected GroundProjectile(@NonNull CombatEntity shooter, int velocity, @NonNull ProjectileOption option) {
        super(shooter, velocity, option);
        if (option.hasGravity)
            throw new IllegalArgumentException("ProjectileOption의 'hasGravity'가 false여야 함");
    }

    /**
     * 지면 고정 투사체 인스턴스를 생성한다.
     *
     * @param shooter  발사자
     * @param velocity 투사체의 속력. (단위: 블록/s). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected GroundProjectile(@NonNull CombatEntity shooter, int velocity) {
        super(shooter, velocity);
    }

    @Override
    @MustBeInvokedByOverriders
    protected boolean onInterval() {
        if (!LocationUtil.isNonSolid(getLocation())) {
            Location shiftLocUp = getLocation().clone();
            for (int k = 1; k <= 16; k++) {
                if (!LocationUtil.isNonSolid(shiftLocUp.add(0, HITBOX_INTERVAL, 0)))
                    continue;

                getLocation().add(0, k * HITBOX_INTERVAL, 0);
                return true;
            }

            return false;
        } else if (LocationUtil.isNonSolid(getLocation().clone().subtract(0, HITBOX_INTERVAL, 0))) {
            Location shiftLocDown = getLocation().clone();
            for (int k = 1; k <= 16; k++) {
                if (LocationUtil.isNonSolid(shiftLocDown.subtract(0, HITBOX_INTERVAL, 0)))
                    continue;

                getLocation().subtract(0, k * HITBOX_INTERVAL - HITBOX_INTERVAL, 0);
                return true;
            }

            return false;
        }

        return super.onInterval();
    }
}
