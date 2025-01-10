package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerCommandPreprocess extends EventListener<PlayerCommandPreprocessEvent> {
    @Getter
    private static final OnPlayerCommandPreprocess instance = new OnPlayerCommandPreprocess();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerCommandPreprocessEvent event) {
        if (!User.fromPlayer(event.getPlayer()).onCommand())
            event.setCancelled(true);
    }
}
