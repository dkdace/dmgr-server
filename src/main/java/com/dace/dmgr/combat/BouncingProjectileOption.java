package com.dace.dmgr.combat;

import lombok.Builder;

/**
 * 튕기는 투사체의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see BouncingProjectile
 */
@Builder
public class BouncingProjectileOption {
    /** 투사체가 튕겼을 때의 속력 계수 */
    @Builder.Default
    final float bounceVelocityMultiplier = 1;
    /** 바닥에 닿았을 때 제거 여부 */
    @Builder.Default
    final boolean destroyOnHitFloor = false;
}
