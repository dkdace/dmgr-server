package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

        if (CombatEntity.fromEntity(entity) == null) {
            if (entity instanceof Player) {
                User user = User.fromPlayer((Player) entity);
                if (user.isInFreeCombat() || user.getGameRoom() != null)
                    event.setCancelled(true);
            }

            return;
        }

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
