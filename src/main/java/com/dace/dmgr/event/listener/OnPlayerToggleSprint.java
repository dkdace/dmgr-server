package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public final class OnPlayerToggleSprint implements Listener {
    @EventHandler
    public static void event(PlayerToggleSprintEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (combatUser != null && combatUser.getCharacterType() != null) {
            CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.SPRINT);

            Bukkit.getServer().getPluginManager().callEvent(newEvent);
        }
    }
}
