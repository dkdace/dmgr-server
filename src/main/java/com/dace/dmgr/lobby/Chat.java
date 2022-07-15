package com.dace.dmgr.lobby;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.data.model.GeneralConfig;
import com.dace.dmgr.data.model.User;
import com.dace.dmgr.util.CooldownManager;
import com.dace.dmgr.util.SoundPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static com.dace.dmgr.system.EntityList.userList;

public class Chat {
    public static void event(AsyncPlayerChatEvent event, User user) {
        event.setCancelled(true);

        if (!user.player.isOp()) {
            if (CooldownManager.getCooldown(user, User.Cooldown.CHAT) > 0) {
                user.player.sendMessage(DMGR.CHAT_WARN_PREFIX + "채팅을 천천히 하십시오.");
                return;
            }
            CooldownManager.setCooldown(user, User.Cooldown.CHAT, GeneralConfig.chatCooldown);
        }

        Bukkit.getServer().broadcastMessage(String.format("<%s> %s", user.player.getDisplayName(), event.getMessage()));
        Bukkit.getOnlinePlayers().forEach((Player player2) -> {
            User user2 = userList.get(player2.getUniqueId());
            SoundPlayer.play(user2.userConfig.getChatSound(), player2, 1000F, 1.414F);
        });
    }
}
