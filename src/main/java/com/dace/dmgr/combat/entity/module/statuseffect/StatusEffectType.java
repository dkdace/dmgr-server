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
    /** 미분류 */
    NONE(StatusRestrictions.NONE),
    /** 속도 증가 */
    SPEED(StatusRestrictions.NONE),
    /** 무적 */
    INVULNERABLE(StatusRestrictions.DAMAGED),
    /** 둔화 */
    SLOW(StatusRestrictions.NONE),
    /** 기절 */
    STUN(StatusRestrictions.MOVE | StatusRestrictions.ACTION),
    /** 속박 */
    SNARE(StatusRestrictions.MOVE | StatusRestrictions.MOVE_SKILL),
    /** 고정 */
    GROUNDING(StatusRestrictions.MOVE_SKILL),
    /** 침묵 */
    SILENCE(StatusRestrictions.SKILL),
    /** 독 */
    POISON(StatusRestrictions.NONE),
    /** 화염 */
    BURNING(StatusRestrictions.NONE),
    /** 회복 차단 */
    HEAL_BLOCK(StatusRestrictions.HEALED)
    ;

    final long restrictions;
}
