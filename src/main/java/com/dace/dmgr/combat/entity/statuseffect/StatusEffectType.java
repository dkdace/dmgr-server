package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.character.jager.JagerTrait;
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
    /** 기절 */
    STUN(Stun.getInstance()),
    /** 빙결 */
    FREEZE(JagerTrait.Freeze.getInstance()),
    /** 속박 */
    SNARE(Snare.getInstance()),
    /** 고정 */
    GROUNDING(Grounding.getInstance()),
    /** 침묵 */
    SILENCE(Silence.getInstance());

    /** 상태 효과 정보 */
    private final StatusEffect statusEffect;
}
