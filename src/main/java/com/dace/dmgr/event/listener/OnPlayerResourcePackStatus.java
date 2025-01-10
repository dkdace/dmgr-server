package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerResourcePackStatus extends EventListener<PlayerResourcePackStatusEvent> {
    @Getter
    private static final OnPlayerResourcePackStatus instance = new OnPlayerResourcePackStatus();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerResourcePackStatusEvent event) {
        User.fromPlayer(event.getPlayer()).onResourcePackStatus(event.getStatus());
    }
}
