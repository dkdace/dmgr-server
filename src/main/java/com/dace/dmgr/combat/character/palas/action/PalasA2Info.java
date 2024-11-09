package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class PalasA2Info extends ActiveSkillInfo<PalasA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 15 * 20;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 40;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 사용 점수 */
    public static final int USE_SCORE = 5;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 25;
    @Getter
    private static final PalasA2Info instance = new PalasA2Info();

    private PalasA2Info() {
        super(PalasA2.class, "생체 나노봇: 알파-X",
                "",
                "§f▍ 바라보는 아군에게 나노봇을 투여하여 일정",
                "§f▍ 시간동안 모든 §5" + TextIcon.NEGATIVE_EFFECT + " 해로운 효과§f에 면역시킵니다.",
                "§f▍ §d생체 나노봇: 아드레날린 §f효과를 덮어씁니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                "",
                "§7§l[2] §f사용");
    }
}
