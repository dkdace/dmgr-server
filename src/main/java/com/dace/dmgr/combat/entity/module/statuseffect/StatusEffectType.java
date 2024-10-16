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
    NONE(CombatRestrictions.NONE),
    /** 속도 증가 */
    SPEED(CombatRestrictions.NONE),
    /** 무적 */
    INVULNERABLE(CombatRestrictions.DAMAGED),
    /** 둔화 */
    SLOW(CombatRestrictions.NONE),
    /** 기절 */
    STUN(CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.USE_ACTION),
    /** 속박 */
    SNARE(CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.ACTION_MOVE),
    /** 고정 */
    GROUNDING(CombatRestrictions.ACTION_MOVE),
    /** 침묵 */
    SILENCE(CombatRestrictions.USE_SKILL | CombatRestrictions.HEAR),

    /** 독 */
    POISON(CombatRestrictions.NONE),
    /** 화염 */
    BURNING(CombatRestrictions.NONE),
    /** 회복 차단 */
    HEAL_BLOCK(CombatRestrictions.HEALED)
    ;

    final long restrictions;
}
