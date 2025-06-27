package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.combat.ability.TextIcon;
import com.dace.dmgr.combat.ability.info.AbilityInfoLore;
import com.dace.dmgr.combat.ability.info.TraitInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class No7T1Info extends TraitInfo<No7T1> {
    /** 최대 보호막 */
    public static final int MAX_SHIELD = 2000;
    /** 초당 감소량 (단위: 블록) */
    public static final double DECREASE_PER_SECOND = 120;

    @Getter
    private static final No7T1Info instance = new No7T1Info();

    private No7T1Info() {
        super(No7T1.class, "충전",
                new AbilityInfoLore(AbilityInfoLore.Section
                        .builder("스킬로 <e:HEAL:보호막>을 얻을 수 있습니다.")
                        .addValueInfo(TextIcon.HEAL, "최대 {0}", ChatColor.YELLOW, MAX_SHIELD)
                        .build()));
    }
}
