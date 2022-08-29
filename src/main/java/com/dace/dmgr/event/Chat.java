package com.dace.dmgr.event;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownManager;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.dace.dmgr.system.EntityList.userList;

public class Chat {
    public static void event(AsyncPlayerChatEvent event, Player player) {
        User user = userList.get(player.getUniqueId());

        event.setCancelled(true);

        if (!player.isOp()) {
            if (CooldownManager.getCooldown(user, Cooldown.CHAT) > 0) {
                player.sendMessage(DMGR.PREFIX.CHAT_WARN + "채팅을 천천히 하십시오.");
                return;
            }
            CooldownManager.setCooldown(user, Cooldown.CHAT);
        }

        Bukkit.getServer().broadcastMessage(String.format("<%s> %s", player.getDisplayName(), event.getMessage()));
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            User user2 = userList.get(player2.getUniqueId());
            SoundPlayer.play(user2.getUserConfig().getChatSound(), player2, 1000F, 1.414F);
        });
    }
}
