package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

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
        super(ArkaceA1.class, "D.I.A. 코어 미사일",
                "",
                "§f▍ 소형 미사일을 연속으로 발사하여 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f▍ 를 입힙니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1} ~ {2} (폭발)", TextIcon.DAMAGE, DAMAGE_EXPLODE, DAMAGE_EXPLODE / 2),
                MessageFormat.format("§c{0}§f {1} (직격)", TextIcon.DAMAGE, DAMAGE_DIRECT),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[2] [좌클릭] §f사용");
    }
}
