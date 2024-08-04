package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.MessageFormat;

public final class OnAsyncPlayerChat implements Listener {
    /** 쿨타임 ID */
    private static final String COOLDOWN_ID = "Chat";
    /** 채팅의 메시지 포맷 */
    private static final String CHAT_FORMAT = "<{0}> {1}";

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

        Bukkit.getServer().getConsoleSender().sendMessage(MessageFormat.format(CHAT_FORMAT, userData.getDisplayName(), event.getMessage()));

        if (user.getMessageTarget() == null) {
            GameUser gameUser = GameUser.fromUser(user);

            if (gameUser == null || gameUser.getGame().getPhase() == Game.Phase.WAITING) {
                Bukkit.getOnlinePlayers().forEach((Player target) ->
                        sendMessage(user, User.fromPlayer(target), MessageFormat.format(CHAT_FORMAT, userData.getDisplayName(), event.getMessage())));
            } else
                gameUser.sendMessage(event.getMessage(), gameUser.isTeamChat());
        } else {
            sendMessage(user, user, MessageFormat.format(CHAT_FORMAT, userData.getDisplayName(), "§7" + event.getMessage()));
            sendMessage(user, user.getMessageTarget(), MessageFormat.format(CHAT_FORMAT,
                    userData.getDisplayName() + " §7님의 개인 메시지§f", ChatColor.GRAY + event.getMessage()));
        }
    }

    /**
     * 대상 플레이어에게 메시지를 전송하고 효과음을 재생한다.
     *
     * @param sender   발신 플레이어
     * @param receiver 수신 플레이어
     * @param message  메시지
     */
    private static void sendMessage(@NonNull User sender, @NonNull User receiver, @NonNull String message) {
        UserData receiverUserData = receiver.getUserData();

        if (receiverUserData.isBlockedPlayer(sender.getUserData()))
            return;

        receiver.getPlayer().sendMessage(message);
        SoundUtil.play(receiverUserData.getConfig().getChatSound().getSound(), receiver.getPlayer(), 1000, Math.sqrt(2));
    }
}
