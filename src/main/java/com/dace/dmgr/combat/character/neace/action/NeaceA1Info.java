package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class NeaceA1Info extends ActiveSkillInfo<NeaceA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 10 * 20;
    /** 초당 치유량 */
    public static final int HEAL_PER_SECOND = 120;
    /** 최대 치유량 */
    public static final int MAX_HEAL = 600;
    /** 최대 거리 (단위: 블록) */
    public static final int MAX_DISTANCE = 30;
    /** 지속시간 (tick) */
    public static final long DURATION = 15 * 20;
    @Getter
    private static final NeaceA1Info instance = new NeaceA1Info();

    private NeaceA1Info() {
        super(NeaceA1.class, "구원의 표식",
                "",
                "§f▍ 바라보는 아군에게 표식을 남겨 일정 시간동안",
                "§f▍ §a" + TextIcon.HEAL + " 치유§f합니다.",
                "§f▍ 이미 표식이 있는 아군에게 사용할 수 없으며,",
                "§f▍ 치유량이 최대치에 도달하거나 지속 시간이",
                "§f▍ 지나면 사라집니다.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§7{0}§f {1}초", TextIcon.DURATION, DURATION / 20.0),
                MessageFormat.format("§a{0}§f {1}/초 / 최대 {2}", TextIcon.HEAL, HEAL_PER_SECOND, MAX_HEAL),
                MessageFormat.format("§a{0}§f {1}m", TextIcon.DISTANCE, MAX_DISTANCE),
                "",
                "§7§l[1] §f사용");
    }
}
