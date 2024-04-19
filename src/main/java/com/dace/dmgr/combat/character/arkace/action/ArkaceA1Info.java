package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;
import lombok.NonNull;

public final class ArkaceA1Info extends ActiveSkillInfo {
    /** 쿨타임 */
    public static final long COOLDOWN = 7 * 20;
    /** 피해량 (폭발) */
    public static final int DAMAGE_EXPLODE = 100;
    /** 피해량 (직격) */
    public static final int DAMAGE_DIRECT = 50;
    /** 투사체 속력 */
    public static final int VELOCITY = 60;
    /** 피해 범위 */
    public static final double RADIUS = 3;
    @Getter
    private static final ArkaceA1Info instance = new ArkaceA1Info();

    public ArkaceA1Info() {
        super(1, "다이아코어 미사일",
                "",
                "§f소형 미사일을 3회 연속으로 발사하여 §c" + TextIcon.DAMAGE + " 광역 피해",
                "§f를 입힙니다.",
                "",
                "§c" + TextIcon.DAMAGE + "§f 폭발 " + DAMAGE_EXPLODE + " + 직격 " + DAMAGE_DIRECT + "  §c" + TextIcon.RADIUS + "§f 3m",
                "§f" + TextIcon.COOLDOWN + "§f 7초",
                "",
                "§7§l[2] [좌클릭] §f사용");
    }

    @Override
    public @NonNull ArkaceA1 createSkill(@NonNull CombatUser combatUser) {
        return new ArkaceA1(combatUser);
    }
}
