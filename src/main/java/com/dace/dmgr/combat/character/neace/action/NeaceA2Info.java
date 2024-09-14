package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class NeaceA2Info extends ActiveSkillInfo<NeaceA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 12 * 20;
    /** 공격력 증가량 */
    public static final int DAMAGE_INCREMENT = 10;
    /** 방어력 증가량 */
    public static final int DEFENSE_INCREMENT = 10;
    /** 이동속도 증가량 */
    public static final int SPEED = 10;
    /** 지속시간 (tick) */
    public static final long DURATION = 8 * 20;

    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 20;
    @Getter
    private static final NeaceA2Info instance = new NeaceA2Info();

    private NeaceA2Info() {
        super(NeaceA2.class, "축복",
                "",
                "§f▍ 일정 시간동안 기본 무기의 치유 대상을 §3축복§f할",
                "§f▍ 수 있습니다.",
                "§f▍ 사용 중에는 기본 무기의 치유량이 절반으로",
                "§f▍ 감소합니다.",
                "",
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                "",
                "§7§l[2] §f사용",
                "",
                "§3[축복]",
                "",
                "§f▍ §c" + TextIcon.DAMAGE_INCREASE + " 공격력§f과 §6" + TextIcon.DEFENSE_INCREASE + " 방어력§f, §b" + TextIcon.WALK_SPEED_INCREASE + " 이동 속도§f가",
                "§f▍ 증가합니다.",
                "",
                MessageFormat.format("§c{0}§f {1}%", TextIcon.DAMAGE_INCREASE, DAMAGE_INCREMENT),
                MessageFormat.format("§6{0}§f {1}%", TextIcon.DEFENSE_INCREASE, DEFENSE_INCREMENT),
                MessageFormat.format("§b{0}§f {1}%", TextIcon.WALK_SPEED_INCREASE, SPEED),
                "",
                "§3[지속시간 종료/재사용 시]",
                "",
                "§f▍ 사용을 종료합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                "",
                "§7§l[2] §f해제");
    }
}
