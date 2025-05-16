package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.action.info.ActionInfoLore;
import com.dace.dmgr.combat.action.info.ActionInfoLore.Section.Format;
import com.dace.dmgr.combat.action.info.PassiveSkillInfo;
import lombok.Getter;
import org.bukkit.ChatColor;

public final class No7P1Info extends PassiveSkillInfo<No7P1> {
    /** 쿨타임 */
    public static final Timespan COOLDOWN = Timespan.ofSeconds(6);
    /** 보호막 */
    public static final int SHIELD = 300;

    @Getter
    private static final No7P1Info instance = new No7P1Info();

    private No7P1Info() {
        super(No7P1.class, "긴급 보호막",
                new ActionInfoLore(ActionInfoLore.Section
                        .builder("치명상 상태가 되면 <e:HEAL:보호막>을 얻습니다.")
                        .addValueInfo(TextIcon.COOLDOWN, Format.TIME, COOLDOWN.toSeconds())
                        .addValueInfo(TextIcon.HEAL, ChatColor.YELLOW, SHIELD)
                        .build()));
    }
}
