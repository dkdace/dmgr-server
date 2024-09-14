package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class VellionA1Info extends ActiveSkillInfo<VellionA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 6 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (1.7 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.5 * 20);
    /** 시전 중 이동속도 감소량 */
    public static final int READY_SLOW = 70;
    /** 초당 독 피해량 */
    public static final int POISON_DAMAGE_PER_SECOND = 120;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 160;
    /** 효과 지속 시간 (tick) */
    public static final long EFFECT_DURATION = (long) (2.5 * 20);
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.1 * 20);
    /** 회수 시간 (tick) */
    public static final long RETURN_DURATION = (long) (0.75 * 20);
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;

    /** 효과 점수 */
    public static final int EFFECT_SCORE = 1;
    @Getter
    private static final VellionA1Info instance = new VellionA1Info();

    private VellionA1Info() {
        super(VellionA1.class, "마력 집중",
                "",
                "§f▍ 개체를 관통하는 마력 응집체를 날려 적에게는",
                "§f▍ §c" + TextIcon.POISON + " 독 피해§f와 짧은 §5" + TextIcon.SNARE + " 속박§f을 입히고,",
                "§f▍ 아군에게는 지속적인 §a" + TextIcon.HEAL + " 치유 §f효과를 줍니다.",
                "§f▍ 벽이나 최대 사거리에 도달하면 되돌아오며 효과를",
                "§f▍ 다시 입힙니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§f{0} {1}m", TextIcon.DISTANCE, VELOCITY * RETURN_DURATION / 20.0),
                MessageFormat.format("§f{0} {1}m", TextIcon.RADIUS, RADIUS),
                MessageFormat.format("§c{0}§f {1}초 / {2}/초", TextIcon.POISON, EFFECT_DURATION / 20.0, POISON_DAMAGE_PER_SECOND),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.SNARE, SNARE_DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}초 / {2}/초", TextIcon.HEAL, EFFECT_DURATION / 20.0, HEAL_PER_SECOND),
                "",
                "§7§l[1] [우클릭] §f사용");
    }
}
