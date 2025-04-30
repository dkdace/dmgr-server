package com.dace.dmgr.combat.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * 무기 및 스킬 설명의 텍스트 아이콘 목록.
 */
@AllArgsConstructor
@Getter
public enum TextIcon {
    /** 피해량 */
    DAMAGE('\u4DC0', ChatColor.RED),
    /** 공격력 증가 */
    DAMAGE_INCREASE('\u4DC1', ChatColor.RED),
    /** 공격력 감소 */
    DAMAGE_DECREASE('\u4DC2', ChatColor.RED),
    /** 공격속도 */
    ATTACK_SPEED('\u4DC3', ChatColor.RED),
    /** 치유량 */
    HEAL('\u4DC4', ChatColor.GREEN),
    /** 치유량 증가 */
    HEAL_INCREASE('\u4DC5', ChatColor.GREEN),
    /** 치유량 감소 */
    HEAL_DECREASE('\u4DC6', ChatColor.GREEN),
    /** 치유 차단 */
    HEAL_BAN('\u4DC7', ChatColor.DARK_PURPLE),
    /** 장탄수 */
    CAPACITY('\u4DC8', ChatColor.WHITE),
    /** 방어력 */
    DEFENSE('\u4DC9', ChatColor.GOLD),
    /** 방어력 증가 */
    DEFENSE_INCREASE('\u4DCA', ChatColor.GOLD),
    /** 방어력 감소 */
    DEFENSE_DECREASE('\u4DCB', ChatColor.GOLD),
    /** 화염 */
    FIRE('\u4DCC', ChatColor.RED),
    /** 지속시간 */
    DURATION('\u4DCD', ChatColor.GRAY),
    /** 지속시간 감소 */
    DURATION_DECREASE('\u4DCE', ChatColor.GRAY),
    /** 궁극기 충전량 */
    ULTIMATE('\u4DCF', ChatColor.WHITE),
    /** 이동속도 */
    WALK_SPEED('\u4DD0', ChatColor.AQUA),
    /** 이동속도 증가 */
    WALK_SPEED_INCREASE('\u4DD1', ChatColor.AQUA),
    /** 이동속도 감소 */
    WALK_SPEED_DECREASE('\u4DD2', ChatColor.AQUA),
    /** 독 */
    POISON('\u4DD3', ChatColor.RED),
    /** 투명화 */
    INVISIBLE('\u4DD4', ChatColor.WHITE),
    /** 쿨타임 */
    COOLDOWN('\u4DD5', ChatColor.WHITE),
    /** 쿨타임 감소 */
    COOLDOWN_DECREASE('\u4DD6', ChatColor.WHITE),
    /** 침묵 */
    SILENCE('\u4DD7', ChatColor.DARK_PURPLE),
    /** 실명 */
    BLINDNESS('\u4DD8', ChatColor.DARK_PURPLE),
    /** 해로운 효과 */
    NEGATIVE_EFFECT('\u4DD9', ChatColor.DARK_PURPLE),
    /** 기절 */
    STUN('\u4DDA', ChatColor.DARK_PURPLE),
    /** 넉백 */
    KNOCKBACK('\u4DDB', ChatColor.DARK_PURPLE),
    /** 속박 */
    SNARE('\u4DDC', ChatColor.DARK_PURPLE),
    /** 고정 */
    GROUNDING('\u4DDD', ChatColor.DARK_PURPLE),
    /** 거리 */
    DISTANCE('\u4DDE', ChatColor.WHITE),
    /** 범위 */
    RADIUS('\u4DDF', ChatColor.WHITE),
    /** 생명력 */
    HEALTH('\u4DE0', ChatColor.GREEN),
    /** 생명력 증가 */
    HEALTH_INCREASE('\u4DE1', ChatColor.GREEN),
    /** 생명력 감소 */
    HEALTH_DECREASE('\u4DE2', ChatColor.RED),
    /** 글리치 (델타 고유 자원) */
    GLITCH('\u4DE3', ChatColor.GREEN),
    /** 글리치 감소 */
    GLITCH_DECREASE('\u4DE4', ChatColor.GREEN)
    ;

    /** 아이콘 문자 */
    private final char icon;
    /** 문자 기본 색상 */
    private final ChatColor defaultColor;

    @Override
    public String toString() {
        return String.valueOf(icon);
    }
}
