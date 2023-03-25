package com.dace.dmgr.combat;

import lombok.Builder;

/**
 * 히트스캔의 선택적 옵션을 관리하는 빌더 클래스.
 *
 * @see Hitscan
 */
@Builder
public class HitscanOption {
    /** 관통 여부 */
    @Builder.Default
    boolean penetrating = false;
    /** 판정 반경의 배수 (판정의 엄격함에 영향을 미침) */
    @Builder.Default
    float hitboxMultiplier = 1;
}
