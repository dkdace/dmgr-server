package com.dace.dmgr.combat;

import lombok.Builder;

@Builder
public class HitscanOption {
    @Builder.Default
    boolean penetrating = false;
    @Builder.Default
    float hitboxMultiplier = 1;
}
