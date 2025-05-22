package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.DynamicTraitInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class No7T1Info extends DynamicTraitInfo<No7T1> {
    /** 최대 보호막 */
    public static final int MAX_SHIELD = 2000;
    /** 초당 감소량 (단위: 블록) */
    public static final double DECREASE_PER_SECOND = 120;

    @Getter
    private static final No7T1Info instance = new No7T1Info();

    private No7T1Info() {
        super(No7T1.class, "충전",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("스킬로 <e:HEAL:보호막>을 얻을 수 있습니다.")
                        .addValueInfo(TextIcon.HEAL, "최대 {0}", ChatColor.YELLOW, MAX_SHIELD)
                        .build()));
    }
}
