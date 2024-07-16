package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.CooldownUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class OnPlayerCommandPreprocess implements Listener {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Command";

    @EventHandler
    public static void event(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);

        if (event.getMessage().equals("spawn"))
            event.setCancelled(true);

        if (!player.isOp()) {
            if (CooldownUtil.getCooldown(user, COOLDOWN_ID) > 0) {
                user.sendMessageWarn("동작이 너무 빠릅니다.");
                event.setCancelled(true);
                return;
            }
            CooldownUtil.setCooldown(user, COOLDOWN_ID, GeneralConfig.getConfig().getCommandCooldown());
        }
    }
}
