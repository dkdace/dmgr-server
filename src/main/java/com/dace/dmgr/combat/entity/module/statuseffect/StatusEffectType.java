package com.dace.dmgr.combat.entity.module.statuseffect;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 상태 효과의 종류 목록.
 *
 * @see StatusEffect
 */
@AllArgsConstructor
@Getter
public enum StatusEffectType {
    /** 속도 증가 */
    SPEED(Speed.getInstance()),
    /** 둔화 */
    SLOW(Slow.getInstance()),
    /** 기절 */
    STUN(Stun.getInstance()),
    /** 속박 */
    SNARE(Snare.getInstance()),
    /** 고정 */
    GROUNDING(Grounding.getInstance()),
    /** 침묵 */
    SILENCE(Silence.getInstance());

    /** 기본 상태 효과 정보 */
    private final StatusEffect statusEffect;
}
