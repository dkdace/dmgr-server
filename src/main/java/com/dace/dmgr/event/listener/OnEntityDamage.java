package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnEntityDamage extends EventListener<EntityDamageEvent> {
    @Getter
    private static final OnEntityDamage instance = new OnEntityDamage();

    @Override
    @EventHandler
    protected void onEvent(@NonNull EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (CombatEntity.fromEntity(entity) != null) {
            event.setCancelled(true);
            return;
        }

        if (entity instanceof Player)
            switch (event.getCause()) {
                case ENTITY_ATTACK:
                case FALL:
                case SUFFOCATION:
                case DROWNING:
                case CRAMMING:
                    event.setCancelled(true);
                    break;
                default:
                    break;
            }
    }
}
