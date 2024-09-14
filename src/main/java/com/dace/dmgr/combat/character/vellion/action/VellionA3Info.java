package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionA3Info extends ActiveSkillInfo<VellionA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 17 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.6 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 6 * 20;

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 2;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;
    @Getter
    private static final VellionA3Info instance = new VellionA3Info();

    private VellionA3Info() {
        super(VellionA3.class, "칠흑의 균열",
                "",
                "§f▍ 바라보는 곳에 균열을 일으켜 범위의 적을",
                "§f▍ §5" + TextIcon.SILENCE + " 침묵§f시키고 §5" + TextIcon.HEAL_BAN + " 회복을 차단§f합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§5{0}§f {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                MessageFormat.format("§5{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[3] §f사용");
    }
}
