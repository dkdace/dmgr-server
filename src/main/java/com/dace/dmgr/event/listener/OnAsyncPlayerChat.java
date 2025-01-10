package com.dace.dmgr.event.listener;

import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnAsyncPlayerChat extends EventListener<AsyncPlayerChatEvent> {
    @Getter
    private static final OnAsyncPlayerChat instance = new OnAsyncPlayerChat();
    /** 채팅의 메시지 포맷 패턴 */
    private static final String CHAT_FORMAT_PATTERN = "<{0}> {1}";

    @Override
    @EventHandler
    protected void onEvent(@NonNull AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        String message = event.getMessage();
        User user = User.fromPlayer(event.getPlayer());
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser == null || gameUser.getGame().getPhase() == Game.Phase.WAITING)
            user.onChat(message);
        else
            gameUser.sendMessage(message, gameUser.isTeamChat());

        Bukkit.getConsoleSender().sendMessage(MessageFormat.format(CHAT_FORMAT_PATTERN, user.getUserData().getDisplayName(), message));
    }
}
