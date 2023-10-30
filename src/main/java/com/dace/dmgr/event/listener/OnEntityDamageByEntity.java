package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class OnEntityDamageByEntity implements Listener {
    @EventHandler
    public static void event(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity victim = event.getEntity();
        CombatEntity attCombatEntity = null;
        CombatEntity vicCombatEntity = null;
        if (attacker instanceof LivingEntity)
            attCombatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) attacker);
        if (victim instanceof LivingEntity)
            vicCombatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) victim);

        if (attCombatEntity != null || vicCombatEntity != null)
            event.setCancelled(true);
        if (attCombatEntity instanceof Attacker && vicCombatEntity instanceof Damageable)
            ((Attacker) attCombatEntity).onDefaultAttack((Damageable) vicCombatEntity);
    }
}

