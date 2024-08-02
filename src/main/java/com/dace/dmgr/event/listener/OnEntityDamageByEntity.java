package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class OnEntityDamageByEntity implements Listener {
    @EventHandler
    public static void event(EntityDamageByEntityEvent event) {
        CombatEntity attacker = CombatEntity.fromEntity(event.getDamager());
        CombatEntity victim = CombatEntity.fromEntity(event.getEntity());
        if (attacker == null || victim == null)
            return;

        event.setCancelled(true);
        if (attacker instanceof Attacker && victim instanceof Damageable)
            ((Attacker) attacker).onDefaultAttack((Damageable) victim);
    }
}

