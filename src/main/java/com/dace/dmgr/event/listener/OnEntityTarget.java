package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnEntityTarget extends EventListener<EntityTargetEvent> {
    @Getter
    private static final OnEntityTarget instance = new OnEntityTarget();

    @Override
    @EventHandler
    protected void onEvent(@NonNull EntityTargetEvent event) {
        CombatEntity combatEntity = CombatEntity.fromEntity(event.getEntity());

        if (combatEntity != null)
            event.setCancelled(true);
    }
}
