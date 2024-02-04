package com.dace.dmgr.event.listener;

import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public final class OnTabComplete implements Listener {
    @EventHandler
    public static void event(TabCompleteEvent event) {
        if (event.getSender() instanceof Player) {
            Player player = (Player) event.getSender();

            if (!player.isOp() && event.getBuffer().split(" ").length == 1) {
                User.fromPlayer(player).sendMessageWarn("금지된 행동입니다.");
                event.setCancelled(true);
            }
        }
    }
}
