package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Sittable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerInteractEntity extends EventListener<PlayerInteractEntityEvent> {
    @Getter
    private static final OnPlayerInteractEntity instance = new OnPlayerInteractEntity();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Sittable)
            event.setCancelled(true);
    }
}
