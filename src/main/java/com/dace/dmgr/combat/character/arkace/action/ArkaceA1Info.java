package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class ArkaceA1Info extends ActiveSkillInfo<ArkaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 7 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.5 * 20);
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 120;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 60;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.25;

    /** 직격 점수 */
    public static final int DIRECT_HIT_SCORE = 3;
    @Getter
    private static final ArkaceA1Info instance = new ArkaceA1Info();

    private ArkaceA1Info() {
        super(ArkaceA1.class, "다이아코어 미사일",
                "",
                "§f소형 미사일을 3회 연속으로 발사하여 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f를 입힙니다.",
                "",
                "§c" + TextIcon.DAMAGE + "§f 폭발 " + DAMAGE_EXPLODE + " + 직격 " + DAMAGE_DIRECT + "  §c" + TextIcon.RADIUS + "§f 3m",
                "§f" + TextIcon.COOLDOWN + "§f 7초",
                "", "§7§l[2] [좌클릭] §f사용");
    }
}
