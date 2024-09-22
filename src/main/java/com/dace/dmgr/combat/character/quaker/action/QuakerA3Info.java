package com.dace.dmgr.combat.character.quaker.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class QuakerA3Info extends ActiveSkillInfo<QuakerA3> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.8 * 20);
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.4 * 20);
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 40;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 20;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 1.2;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2;
    /** 속박 시간 (tick) */
    public static final long SNARE_DURATION = (long) (0.8 * 20);
    /** 넉백 강도 */
    public static final double KNOCKBACK = 1.5;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 5;
    @Getter
    private static final QuakerA3Info instance = new QuakerA3Info();

    private QuakerA3Info() {
        super(QuakerA3.class, "돌풍 강타",
                "",
                "§f▍ 검기를 날려 처음 맞은 적을 크게 §5" + TextIcon.KNOCKBACK + " 밀쳐내고",
                "§f▍ §c" + TextIcon.DAMAGE + " 피해§f와 §5" + TextIcon.SNARE + " 속박§f을 입힙니다.",
                "§f▍ 적이 날아가며 부딪힌 적에게도 같은 효과를",
                "§f▍ 입힙니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                MessageFormat.format("§5{0}§f {1}초", TextIcon.SNARE, SNARE_DURATION / 20.0),
                "",
                "§7§l[3] §f사용");
    }
}
