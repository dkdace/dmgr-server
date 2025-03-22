package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.TabCompleteEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnTabComplete extends EventListener<TabCompleteEvent> {
    @Getter
    private static final OnTabComplete instance = new OnTabComplete();

    @Override
    @EventHandler
    protected void onEvent(@NonNull TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player))
            return;

        Player sender = (Player) event.getSender();

        if (!sender.isOp() && !event.getBuffer().contains(" ")) {
            User.fromPlayer(sender).sendMessageWarn("금지된 행동입니다.");
            event.setCancelled(true);
        }
    }
}
