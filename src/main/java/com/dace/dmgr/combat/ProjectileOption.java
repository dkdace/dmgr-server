package com.dace.dmgr.combat;

import lombok.Builder;

@Builder
public class ProjectileOption {
    @Builder.Default
    boolean penetrating = false;
    @Builder.Default
    float hitboxMultiplier = 1;
    @Builder.Default
    boolean hasGravity = false;
    @Builder.Default
    int bouncing = 0;
}
