package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.character.jager.action.JagerA1Entity;
import com.dace.dmgr.combat.character.jager.action.JagerA1Info;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.statuseffect.Snare;
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
        CombatEntity<?> attCombatEntity = null;
        CombatEntity<?> vicCombatEntity = null;
        if (attacker instanceof LivingEntity)
            attCombatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) attacker);
        if (victim instanceof LivingEntity)
            vicCombatEntity = EntityInfoRegistry.getCombatEntity((LivingEntity) victim);

        if (attCombatEntity != null || vicCombatEntity != null)
            event.setCancelled(true);
        if (vicCombatEntity != null) {
            if (attCombatEntity instanceof JagerA1Entity) {
                vicCombatEntity.damage(attCombatEntity, JagerA1Info.DAMAGE, "", vicCombatEntity.hasStatusEffect(Snare.getInstance()), true);
            }
        }
    }
}

