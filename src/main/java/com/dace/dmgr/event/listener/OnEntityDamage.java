package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class OnEntityDamage implements Listener {
    @EventHandler
    public static void event(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity))
            return;

        CombatEntity<?> combatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) entity);

        if (combatEntity != null) {
            switch (event.getCause()) {
                case FALL:
                case POISON:
                case WITHER:
                case SUFFOCATION:
                case ENTITY_EXPLOSION:
                case DROWNING:
                case FIRE_TICK:
                    event.setCancelled(true);
            }
        }

        ((LivingEntity) entity).setNoDamageTicks(1);
        ((LivingEntity) entity).setMaximumNoDamageTicks(1);
    }
}
