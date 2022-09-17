package com.dace.dmgr.event.listener;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownManager;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.dace.dmgr.system.HashMapList.userHashMap;

public class OnAsyncPlayerChat implements Listener {
    @EventHandler
    public static void event(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = userHashMap.get(player);

        if (!player.isOp()) {
            if (CooldownManager.getCooldown(user, Cooldown.CHAT) > 0) {
                player.sendMessage(DMGR.PREFIX.CHAT_WARN + "채팅을 천천히 하십시오.");
                return;
            }
            CooldownManager.setCooldown(user, Cooldown.CHAT);
        }

        Bukkit.getServer().broadcastMessage(String.format("<%s> %s", player.getDisplayName(), event.getMessage()));
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            User user2 = userHashMap.get(player2);
            SoundPlayer.play(user2.getUserConfig().getChatSound().getSound(), player2, 1000F, 1.414F);
        });
    }
}
