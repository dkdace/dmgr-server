package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class OnEntityDamageByEntity implements Listener {
    @EventHandler
    public static void event(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();
        CombatEntity attCombatEntity = CombatEntity.fromEntity(attacker);
        CombatEntity vicCombatEntity = CombatEntity.fromEntity(victim);
        if (attCombatEntity == null || vicCombatEntity == null)
            return;

        event.setCancelled(true);
        if (attCombatEntity instanceof Attacker && vicCombatEntity instanceof Damageable)
            ((Attacker) attCombatEntity).onDefaultAttack((Damageable) vicCombatEntity);
    }
}

