package com.dace.dmgr.event;

import com.dace.dmgr.combat.CombatUser;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.EntityList.combatUserList;

public class WeaponShoot {
    public static void event(WeaponPreShootEvent event, Player player) {
        CombatUser combatUser = combatUserList.get(event.getPlayer().getUniqueId());

        if (combatUser != null)
            combatUser.onWeaponShoot();
    }
}
