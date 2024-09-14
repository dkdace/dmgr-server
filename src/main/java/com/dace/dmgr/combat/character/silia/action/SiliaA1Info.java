package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class SiliaA1Info extends ActiveSkillInfo<SiliaA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 8 * 20;
    /** 이동 거리 (단위: 블록) */
    public static final int MOVE_DISTANCE = 15;
    /** 이동 강도 */
    public static final double PUSH = 2.5;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (0.3 * 20);
    /** 피해량 */
    public static final int DAMAGE = 250;
    /** 사거리 (단위: 블록) */
    public static final double DISTANCE = 3;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 2.5;
    @Getter
    private static final SiliaA1Info instance = new SiliaA1Info();

    private SiliaA1Info() {
        super(SiliaA1.class, "연풍 가르기",
                "",
                "§f▍ 앞으로 빠르게 이동하며 §c" + TextIcon.DAMAGE + " 광역 피해§f를 입힙니다.",
                "§f▍ 적을 처치하면 " + TextIcon.COOLDOWN + " §7쿨타임§f이 초기화됩니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§f{0} {1}m", TextIcon.DISTANCE, MOVE_DISTANCE),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[1] §f사용");
    }
}
