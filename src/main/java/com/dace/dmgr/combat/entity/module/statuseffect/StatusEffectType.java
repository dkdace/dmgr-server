package com.dace.dmgr.combat.entity.module.statuseffect;

/**
 * 상태 효과의 유형 목록.
 *
 * @see StatusEffect
 */
public enum StatusEffectType {
    /** 미분류 */
    NONE,
    /** 속도 증가 */
    SPEED,
    /** 무적 */
    INVULNERABLE,
    /** 둔화 */
    SLOW,
    /** 기절 */
    STUN,
    /** 속박 */
    SNARE,
    /** 고정 */
    GROUNDING,
    /** 침묵 */
    SILENCE,
    /** 독 */
    POISON,
    /** 화염 */
    BURNING,
    /** 회복 차단 */
    HEAL_BLOCK
}
