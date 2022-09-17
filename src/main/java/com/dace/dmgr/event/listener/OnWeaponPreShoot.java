package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatUser;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.dace.dmgr.system.HashMapList.combatUserHashMap;

public class OnWeaponPreShoot implements Listener {
    @EventHandler
    public static void event(WeaponPreShootEvent event) {
        CombatUser combatUser = combatUserHashMap.get(event.getPlayer());

        if (combatUser != null)
            combatUser.onWeaponShoot();
    }
}