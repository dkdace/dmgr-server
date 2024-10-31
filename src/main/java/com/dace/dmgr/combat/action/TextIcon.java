package com.dace.dmgr.combat.action;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 무기 및 스킬 설명의 텍스트 아이콘 목록.
 */
@AllArgsConstructor
@Getter
public enum TextIcon {
    /** 피해량 */
    DAMAGE('\u4DC0'),
    /** 공격력 증가 */
    DAMAGE_INCREASE('\u4DC1'),
    /** 공격력 감소 */
    DAMAGE_DECREASE('\u4DC2'),
    /** 공격속도 */
    ATTACK_SPEED('\u4DC3'),
    /** 치유량 */
    HEAL('\u4DC4'),
    /** 치유량 증가 */
    HEAL_INCREASE('\u4DC5'),
    /** 치유량 감소 */
    HEAL_DECREASE('\u4DC6'),
    /** 치유 차단 */
    HEAL_BAN('\u4DC7'),
    /** 장탄수 */
    CAPACITY('\u4DC8'),
    /** 방어력 */
    DEFENSE('\u4DC9'),
    /** 방어력 증가 */
    DEFENSE_INCREASE('\u4DCA'),
    /** 방어력 감소 */
    DEFENSE_DECREASE('\u4DCB'),
    /** 화염 */
    FIRE('\u4DCC'),
    /** 지속시간 */
    DURATION('\u4DCD'),
    /** 지속시간 감소 */
    DURATION_DECREASE('\u4DCE'),
    /** 궁극기 충전량 */
    ULTIMATE('\u4DCF'),
    /** 이동속도 */
    WALK_SPEED('\u4DD0'),
    /** 이동속도 증가 */
    WALK_SPEED_INCREASE('\u4DD1'),
    /** 이동속도 감소 */
    WALK_SPEED_DECREASE('\u4DD2'),
    /** 독 */
    POISON('\u4DD3'),
    /** 투명화 */
    INVISIBLE('\u4DD4'),
    /** 쿨타임 */
    COOLDOWN('\u4DD5'),
    /** 쿨타임 감소 */
    COOLDOWN_DECREASE('\u4DD6'),
    /** 침묵 */
    SILENCE('\u4DD7'),
    /** 실명 */
    BLINDNESS('\u4DD8'),
    /** 해로운 효과 */
    NEGATIVE_EFFECT('\u4DD9'),
    /** 기절 */
    STUN('\u4DDA'),
    /** 넉백 */
    KNOCKBACK('\u4DDB'),
    /** 속박 */
    SNARE('\u4DDC'),
    /** 고정 */
    GROUNDING('\u4DDD'),
    /** 거리 */
    DISTANCE('\u4DDE'),
    /** 범위 */
    RADIUS('\u4DDF');

    /** 아이콘 문자 */
    private final char icon;
}
