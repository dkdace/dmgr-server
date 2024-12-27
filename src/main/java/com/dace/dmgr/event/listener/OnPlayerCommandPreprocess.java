package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class OnPlayerCommandPreprocess implements Listener {
    @EventHandler
    public static void event(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);

        if (event.getMessage().equals("spawn"))
            event.setCancelled(true);

        if (!player.isOp()) {
            if (user.getCommandCooldownTimestamp().isAfter(Timestamp.now())) {
                user.sendMessageWarn("동작이 너무 빠릅니다.");
                event.setCancelled(true);
                return;
            }

            user.setCommandCooldownTimestamp(Timestamp.now().plus(Timespan.ofTicks(GeneralConfig.getConfig().getCommandCooldown())));
        }
    }
}
