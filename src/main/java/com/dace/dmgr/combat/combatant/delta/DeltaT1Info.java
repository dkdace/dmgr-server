package com.dace.dmgr.combat.combatant.delta;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public class DeltaT1Info extends DynamicTraitInfo<DeltaT1> {
    /** 최대치 */
    public static final int MAX = 128;

    @Getter
    private static final DeltaT1Info instance = new DeltaT1Info();

    private DeltaT1Info() {
        super(DeltaT1.class, "글리치",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("수치에 비례하여 기본 무기의 공격력이 증가하는 고유 자원입니다. 피해를 받으면 감소합니다.")
                        .addValueInfo(TextIcon.GLITCH, "최대 " + MAX)
                        .addValueInfo(TextIcon.GLITCH_DECREASE, "(받은 피해) × 0.15", ChatColor.RED)
                        .build()));
    }
}
