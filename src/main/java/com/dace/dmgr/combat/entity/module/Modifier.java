package com.dace.dmgr.combat.entity.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 수정자 값 클래스.
 */
@AllArgsConstructor
@Getter
@Setter
public final class Modifier {
    /** 값 증가량 */
    private double increment;
}
