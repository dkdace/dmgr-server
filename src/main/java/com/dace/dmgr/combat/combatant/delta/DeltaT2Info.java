package com.dace.dmgr.combat.combatant.delta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.TraitInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public class DeltaT2Info extends TraitInfo {
    /** 지속 시간 */
    public static final Timespan DURATION = Timespan.ofSeconds(4);

    @Getter
    private static final DeltaT2Info instance = new DeltaT2Info();

    private DeltaT2Info() {
        super("잠금",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("일정 시간동안 아무것도 할 수 없고, 모든 피해와 치유를 받지 못하는 상태이상입니다.")
                        .addValueInfo(TextIcon.DURATION, DURATION.toSeconds())
                        .build()));
    }
}
