package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class SiliaA2Info extends ActiveSkillInfo<SiliaA2> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 11 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = 1 * 20;
    /** 시전 시간 (tick) */
    public static final long READY_DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 100;
    /** 이동 강도 */
    public static final double PUSH = 0.8;
    /** 사거리 (단위: 블록) */
    public static final int DISTANCE = 15;
    /** 투사체 속력 (단위: 블록/s) */
    public static final int VELOCITY = 25;
    /** 투사체 크기 (단위: 블록) */
    public static final double SIZE = 0.8;

    /** 피해 점수 */
    public static final int DAMAGE_SCORE = 8;
    @Getter
    private static final SiliaA2Info instance = new SiliaA2Info();

    private SiliaA2Info() {
        super(SiliaA2.class, "진권풍",
                "",
                "§f▍ 회오리바람을 날려 적에게 §c" + TextIcon.DAMAGE + " 피해§f를 입히고",
                "§f▍ §5" + TextIcon.KNOCKBACK + " 공중에 띄웁니다§f. 적중 시 맞은 적의 뒤로",
                "§f▍ 순간이동합니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.DISTANCE, DISTANCE),
                "",
                "§7§l[2] [우클릭] §f사용");
    }
}
