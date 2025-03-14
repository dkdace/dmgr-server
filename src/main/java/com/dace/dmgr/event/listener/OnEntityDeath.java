package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.temporary.TemporaryEntity;
import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnEntityDeath extends EventListener<EntityDeathEvent> {
    @Getter
    private static final OnEntityDeath instance = new OnEntityDeath();

    @Override
    @EventHandler
    protected void onEvent(@NonNull EntityDeathEvent event) {
        CombatEntity combatEntity = CombatEntity.fromEntity(event.getEntity());

        if (combatEntity instanceof TemporaryEntity)
            combatEntity.remove();
    }
}
