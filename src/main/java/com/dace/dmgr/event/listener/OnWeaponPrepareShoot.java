package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class OnWeaponPrepareShoot implements Listener {
    @EventHandler
    public static void event(WeaponPrepareShootEvent event) {
        CombatUser combatUser = EntityInfoRegistry.getCombatUser(event.getPlayer());

        if (combatUser != null && combatUser.getCharacter() != null) {
            Weapon weapon = combatUser.getWeapon();

            if (!weapon.canUse())
                event.setCancelled(true);
            else {
                CombatUserActionEvent newEvent = new CombatUserActionEvent(combatUser, ActionKey.CS_PRE_USE);

                Bukkit.getServer().getPluginManager().callEvent(newEvent);
                if (weapon instanceof Reloadable && ((Reloadable) weapon).getRemainingAmmo() > 0)
                    ((Reloadable) weapon).cancelReloading();
            }
        }
    }
}