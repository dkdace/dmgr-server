package com.dace.dmgr.combat;

import lombok.Builder;

/**
 * 투사체의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see Projectile
 */
@Builder
public class ProjectileOption {
    /** 트레일 파티클을 남기는 주기. 단위: 판정점 개수 */
    @Builder.Default
    final int trailInterval = 7;
    /** 총알의 최대 사거리. 단위: 블록 */
    @Builder.Default
    final int maxDistance = 70;
    /** 관통 여부 */
    @Builder.Default
    final boolean penetrating = false;
    /** 판정 반경의 배수 (판정의 엄격함에 영향을 미침) */
    @Builder.Default
    final float hitboxMultiplier = 1;
    /** 중력 작용 여부 */
    @Builder.Default
    final boolean hasGravity = false;
}
