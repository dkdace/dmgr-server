package com.dace.dmgr.event;

import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class PlayerToggleSprint {
    public static void event(PlayerToggleSprintEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(player.getUniqueId());

        if (combatUser != null)
            combatUser.onSprintToggle(event.isSprinting());
    }
}
