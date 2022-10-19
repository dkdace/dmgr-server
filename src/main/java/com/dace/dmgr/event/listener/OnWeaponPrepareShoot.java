package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.WeaponController;
import com.dace.dmgr.combat.entity.CombatUser;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class OnWeaponPrepareShoot implements Listener {
    @EventHandler
    public static void event(WeaponPrepareShootEvent event) {
        CombatUser combatUser = combatUserMap.get(event.getPlayer());

        if (combatUser != null && combatUser.getCharacter() != null) {
            WeaponController weaponController = combatUser.getWeaponController();

            if (weaponController.getRemainingAmmo() == 0) {
                event.setCancelled(true);

                if (!weaponController.isReloading())
                    weaponController.reload();
            } else
                weaponController.setReloading(false);
        }
    }
}