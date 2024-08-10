package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class OnEntityDamage implements Listener {
    @EventHandler
    public static void event(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        CombatEntity combatEntity = CombatEntity.fromEntity(entity);
        if (combatEntity == null)
            return;

        switch (event.getCause()) {
            case FALL:
            case POISON:
            case WITHER:
            case SUFFOCATION:
            case ENTITY_EXPLOSION:
            case DROWNING:
            case FIRE_TICK:
                event.setCancelled(true);
                break;
            default:
                break;
        }

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setNoDamageTicks(1);
            ((LivingEntity) entity).setMaximumNoDamageTicks(1);
        }
    }
}
