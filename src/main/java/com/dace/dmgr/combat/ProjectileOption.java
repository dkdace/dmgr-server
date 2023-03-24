package com.dace.dmgr.combat;

import lombok.Builder;

/**
 * 투사체의 선택적 옵션을 관리하는 빌더 클래스.
 */
@Builder
public class ProjectileOption {
    /** 관통 여부 */
    @Builder.Default
    boolean penetrating = false;
    /** 판정 반경의 배수 (판정의 엄격함에 영향을 미침) */
    @Builder.Default
    float hitboxMultiplier = 1;
    /** 중력 작용 여부 */
    @Builder.Default
    boolean hasGravity = false;
    /** 투사체가 튕기는 횟수 {@code 0}으로 설정 시 튕기지 않음 */
    @Builder.Default
    int bouncing = 0;
}
