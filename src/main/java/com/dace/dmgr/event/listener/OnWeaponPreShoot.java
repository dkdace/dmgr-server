package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnWeaponPreShoot implements Listener {
    @EventHandler
    public static void event(WeaponPreShootEvent event) {
        CombatUser combatUser = combatUserMap.get(event.getPlayer());

        if (combatUser != null && combatUser.getCharacter() != null) {
            WeaponController weaponController = combatUser.getWeaponController();

            if (weaponController.getRemainingAmmo() == 0 || weaponController.isReloading() || weaponController.isSwapping())
                event.setCancelled(true);
            else {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.CS_USE);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
                weaponController.setReloading(false);
            }
        }
    }
}