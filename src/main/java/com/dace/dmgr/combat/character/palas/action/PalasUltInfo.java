package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.UltimateSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class PalasUltInfo extends UltimateSkillInfo<PalasUlt> {
    /** 궁극기 필요 충전량 */
    public static final int COST = 8000;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 60;
    /** 이동속도 증가량 */
    public static final int SPEED_INCREMENT = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = 7 * 20;

    /** 사용 점수 */
    public static final int USE_SCORE = 10;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 30;
    @Getter
    private static final PalasUltInfo instance = new PalasUltInfo();

    private PalasUltInfo() {
        super(PalasUlt.class, "생체 나노봇: 아드레날린",
                "",
                "§f▍ 바라보는 아군에게 나노봇을 투여하여 일정",
                "§f▍ 시간동안 §c" + TextIcon.DAMAGE_INCREASE + " 공격력§f과 §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f를",
                "§f▍ 증폭시킵니다.",
                "§f▍ §d생체 나노봇: 스팀 알파-X §f효과를 덮어씁니다.",
                "",
                MessageFormat.format("§f{0} {1}", TextIcon.ULTIMATE, COST),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}%", TextIcon.DAMAGE_INCREASE, DAMAGE_INCREMENT),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED_INCREMENT),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                "",
                "§7§l[4] §f사용");
    }
}
