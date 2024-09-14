package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

import java.text.MessageFormat;

public final class InfernoA1Info extends ActiveSkillInfo<InfernoA1> {
    /** 쿨타임 (tick) */
    public static final long COOLDOWN = 5 * 20;
    /** 전역 쿨타임 (tick) */
    public static final int GLOBAL_COOLDOWN = (int) (0.4 * 20);
    /** 수직 이동 강도 */
    public static final double PUSH_UP = 0.5;
    /** 수평 이동 강도 */
    public static final double PUSH_SIDE = 1.6;
    /** 피해량 */
    public static final int DAMAGE = 200;
    /** 피해 범위 (단위: 블록) */
    public static final double RADIUS = 3.5;
    /** 넉백 강도 */
    public static final double KNOCKBACK = 0.6;
    @Getter
    private static final InfernoA1Info instance = new InfernoA1Info();

    private InfernoA1Info() {
        super(InfernoA1.class, "점프 부스터",
                "",
                "§f▍ 앞으로 높게 도약하여 착지할 때 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f▍ 를 입히고 §5" + TextIcon.KNOCKBACK + " 밀쳐냅니다§f.",
                "",
                MessageFormat.format("§f{0} {1}초", TextIcon.COOLDOWN, COOLDOWN / 20.0),
                MessageFormat.format("§c{0}§f {1}", TextIcon.DAMAGE, DAMAGE),
                MessageFormat.format("§c{0}§f {1}m", TextIcon.RADIUS, RADIUS),
                "",
                "§7§l[1] §f사용");
    }
}
