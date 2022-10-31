package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnPlayerArmorStandManipulate implements Listener {
    @EventHandler
    public static void event(PlayerArmorStandManipulateEvent event) {
        CombatUser combatUser = combatUserMap.get(event.getPlayer());

        if (combatUser != null) {
            if (combatUser.getCharacter() != null) {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.RIGHT_CLICK);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
            }
        }
    }
}
