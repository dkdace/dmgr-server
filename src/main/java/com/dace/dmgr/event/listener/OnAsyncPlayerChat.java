package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
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
import java.util.ArrayList;

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
            GameUser gameUser = GameUser.fromUser(user);

            if (gameUser == null) {
                Bukkit.getOnlinePlayers().forEach((Player player2) ->
                        sendMessage(user, User.fromPlayer(player2), MessageFormat.format("<{0}> {1}", userData.getDisplayName(), event.getMessage())));
            } else if (gameUser.getGame().getPhase() != Game.Phase.READY && gameUser.getGame().getPhase() != Game.Phase.PLAYING) {
                CombatUser combatUser = CombatUser.fromUser(user);

                ArrayList<GameUser> targets;
                if (gameUser.isTeamChat())
                    targets = gameUser.getGame().getTeamUserMap().get(gameUser.getTeam());
                else
                    targets = gameUser.getGame().getGameUsers();

                targets.forEach(gameUser2 -> sendMessage(user, gameUser2.getUser(), MessageFormat.format("§7§l[{0}] §f<{1}§l[{2}]§f{3}> {4}",
                        gameUser.isTeamChat() ? "팀" : "전체", gameUser.getTeam().getColor(),
                        combatUser == null ? "미선택" : combatUser.getCharacterType().getCharacter().getName(), player.getName(), event.getMessage())));
            }
        } else {
            sendMessage(user, user, MessageFormat.format("<{0}> §7{1}", userData.getDisplayName(), event.getMessage()));
            sendMessage(user, user.getMessageTarget(), MessageFormat.format("<{0} §7님의 개인 메시지§f> §7{1}", userData.getDisplayName(), event.getMessage()));
        }
    }

    /**
     * 대상 플레이어에게 메시지를 전송하고 효과음을 재생한다.
     *
     * @param sender   발신 플레이어
     * @param receiver 수신 플레이어
     * @param message  메시지
     */
    private static void sendMessage(User sender, User receiver, String message) {
        UserData receiverUserData = receiver.getUserData();

        if (receiverUserData.isBlockedPlayer(sender.getUserData()))
            return;

        receiver.getPlayer().sendMessage(message);
        SoundUtil.play(receiverUserData.getConfig().getChatSound().getSound(), receiver.getPlayer(), 1000, Math.sqrt(2));
    }
}
