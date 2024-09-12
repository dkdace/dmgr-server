package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class InfernoA2Info extends ActiveSkillInfo<InfernoA2> {
    /** 쿨타임 (tick) */
    public static final int COOLDOWN = 12 * 20;
    /** 초당 화염 피해량 */
    public static final int FIRE_DAMAGE_PER_SECOND = 60;
    /** 효과 범위 (단위: 블록) */
    public static final double RADIUS = 5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 초당 효과 점수 */
    public static final int EFFECT_SCORE_PER_SECOND = 3;
    /** 처치 지원 점수 */
    public static final int ASSIST_SCORE = 15;
    @Getter
    private static final InfernoA2Info instance = new InfernoA2Info();

    private InfernoA2Info() {
        super(InfernoA2.class, "불꽃 방출",
                "",
                "§f▍ 일정 시간동안 주변에 불꽃을 방출하여",
                "§f▍ §c" + TextIcon.FIRE + " 화염 피해§f를 입히고 §5" + TextIcon.GROUNDING + " 고정§f시킵니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§c{0}§f {1}/초", TextIcon.FIRE, FIRE_DAMAGE_PER_SECOND),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[2] §f사용");
    }
}
