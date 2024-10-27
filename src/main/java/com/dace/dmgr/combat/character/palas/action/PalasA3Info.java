package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class PalasA3Info extends ActiveSkillInfo<PalasA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 16 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 최대 체력 증가 비율 */
    public static final double HEALTH_INCREASE_RATIO = 0.3;
    /** 최대 체력 감소 비율 */
    public static final double HEALTH_DECREASE_RATIO = 0.3;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 35;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = 6 * 20;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 10;
    @Getter
    private static final PalasA3Info instance = new PalasA3Info();

    private PalasA3Info() {
        super(PalasA3.class, "R.S.K. 생체 제어 수류탄",
                "",
                "§f▍ 특수 수류탄을 던져 범위의 적에게는 §c" + TextIcon.HEAL_DECREASE + " 최대 체력",
                "§f▍ 을 감소시키고, 아군에게는 §a" + TextIcon.HEAL_INCREASE + " 최대 체력§f을",
                "§f▍ 증가시킵니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§f{0} {1}m", TextIcon.RADIUS, RADIUS),
                MessageFormat.format("§c{0}§f {1}%", TextIcon.HEAL_DECREASE, 100 * HEALTH_DECREASE_RATIO),
                MessageFormat.format("§a{0}§f {1}%", TextIcon.HEAL_INCREASE, 100 * HEALTH_INCREASE_RATIO),
                "",
                "§7§l[3] §f사용");
    }
}
