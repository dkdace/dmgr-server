package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnEntityDamageByEntity extends EventListener<EntityDamageByEntityEvent> {
    @Getter
    private static final OnEntityDamageByEntity instance = new OnEntityDamageByEntity();

    @Override
    @EventHandler
    protected void onEvent(@NonNull EntityDamageByEntityEvent event) {
        CombatEntity attacker = CombatEntity.fromEntity(event.getDamager());
        CombatEntity victim = CombatEntity.fromEntity(event.getEntity());
        if (attacker == null && victim == null)
            return;

        event.setCancelled(true);

        if (attacker instanceof Attacker && victim instanceof Damageable)
            ((Attacker) attacker).onDefaultAttack((Damageable) victim);
    }
}
