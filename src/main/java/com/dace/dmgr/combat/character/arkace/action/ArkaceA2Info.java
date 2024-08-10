package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import lombok.Getter;

public final class ArkaceA2Info extends ActiveSkillInfo<ArkaceA2> {
    /** 쿨타임 (tick) */
    public static final int COOLDOWN = 12 * 20;
    /** 치유량 */
    public static final int HEAL = 350;
    /** 지속시간 (tick) */
    public static final long DURATION = (long) (2.5 * 20);

    /** 치유 점수 */
    public static final int HEAL_SCORE = 8;
    @Getter
    private static final ArkaceA2Info instance = new ArkaceA2Info();

    private ArkaceA2Info() {
        super(ArkaceA2.class, "생체 회복막",
                "",
                "§6" + TextIcon.DURATION + " 지속시간§f동안 회복막을 활성화하여 §a" + TextIcon.HEAL + " 회복§f합니다.",
                "",
                "§6" + TextIcon.DURATION + "§f 2.5초",
                "§a" + TextIcon.HEAL + "§f 350",
                "§f" + TextIcon.COOLDOWN + "§f 12초",
                "", "§7§l[3] §f사용");
    }
}
