package com.dace.dmgr.combat.interaction;

import lombok.Builder;

/**
 * 튕기는 투사체의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see BouncingProjectile
 */
@Builder
public final class BouncingProjectileOption {
    public static final int BOUNCE_VELOCITY_MULTIPLIER_DEFAULT = 1;
    public static final boolean DESTROY_ON_HIT_FLOOR_DEFAULT = false;

    /** 투사체가 튕겼을 때의 속력 배수 */
    @Builder.Default
    final double bounceVelocityMultiplier = BOUNCE_VELOCITY_MULTIPLIER_DEFAULT;
    /** 바닥에 닿았을 때 제거 여부 */
    @Builder.Default
    final boolean destroyOnHitFloor = DESTROY_ON_HIT_FLOOR_DEFAULT;
}
