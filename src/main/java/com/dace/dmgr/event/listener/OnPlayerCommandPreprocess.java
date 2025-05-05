package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerCommandPreprocess extends EventListener<PlayerCommandPreprocessEvent> {
    @Getter
    private static final OnPlayerCommandPreprocess instance = new OnPlayerCommandPreprocess();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp()) {
            String fullCommand = event.getMessage().substring(1);
            PluginCommand pluginCommand = DMGR.getPlugin().getCommand(fullCommand.trim().split(" ")[0]);

            if (pluginCommand == null) {
                event.setMessage("/ " + fullCommand);
                return;
            }
        }

        if (!User.fromPlayer(player).onCommand())
            event.setCancelled(true);
    }
}
