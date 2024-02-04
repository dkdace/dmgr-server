package com.dace.dmgr.event.listener;

import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.MessageFormat;

public final class OnAsyncPlayerChat implements Listener {
    @EventHandler
    public static void event(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = User.fromPlayer(player);

        if (!player.isOp()) {
            if (CooldownUtil.getCooldown(user, Cooldown.CHAT) > 0) {
                user.sendMessageWarn("채팅을 천천히 하십시오.");
                return;
            }
            CooldownUtil.setCooldown(user, Cooldown.CHAT);
        }

        Bukkit.getServer().broadcastMessage(MessageFormat.format("<{0}> {1}", player.getDisplayName(), event.getMessage()));
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            UserData userData2 = UserData.fromPlayer(player2);
            SoundUtil.play(userData2.getConfig().getChatSound().getSound(), player2, 1000, Math.sqrt(2));
        });
    }
}
