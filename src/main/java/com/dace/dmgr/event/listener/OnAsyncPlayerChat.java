package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.MessageFormat;

public final class OnAsyncPlayerChat implements Listener {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Chat";

    @EventHandler
    public static void event(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        UserData userData = UserData.fromPlayer(player);

        if (!player.isOp()) {
            if (CooldownUtil.getCooldown(user, COOLDOWN_ID) > 0) {
                user.sendMessageWarn("채팅을 천천히 하십시오.");
                return;
            }
            CooldownUtil.setCooldown(user, COOLDOWN_ID, GeneralConfig.getConfig().getChatCooldown());
        }

        Bukkit.getServer().getConsoleSender().sendMessage(MessageFormat.format("<{0}> {1}", userData.getDisplayName(), event.getMessage()));
        if (user.getMessageTarget() == null) {
            Bukkit.getOnlinePlayers().forEach((Player player2) -> {
                UserData userData2 = UserData.fromPlayer(player2);
                if (!userData2.isBlockedPlayer(userData)) {
                    player2.sendMessage(MessageFormat.format("<{0}> {1}", userData.getDisplayName(), event.getMessage()));
                    SoundUtil.play(userData2.getConfig().getChatSound().getSound(), player2, 1000, Math.sqrt(2));
                }
            });
        } else {
            User targetUser = user.getMessageTarget();
            UserData targetUserData = targetUser.getUserData();

            player.sendMessage(MessageFormat.format("<{0}> §7{1}", userData.getDisplayName(), event.getMessage()));
            SoundUtil.play(userData.getConfig().getChatSound().getSound(), player, 1000, Math.sqrt(2));

            if (!targetUserData.isBlockedPlayer(userData)) {
                targetUser.getPlayer().sendMessage(MessageFormat.format("<{0} §7님의 개인 메시지§f> §7{1}", userData.getDisplayName(), event.getMessage()));
                SoundUtil.play(targetUserData.getConfig().getChatSound().getSound(), targetUser.getPlayer(), 1000, Math.sqrt(2));
            }
        }
    }
}
