package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 관리자 채팅 명령어 클래스.
 *
 * @see User#setAdminChat(boolean)
 */
public final class AdminChatCommand extends CommandHandler {
    @Getter
    private static final AdminChatCommand instance = new AdminChatCommand();

    private AdminChatCommand() {
        super("관리자");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);

        user.setAdminChat(!user.isAdminChat());
        user.sendMessageInfo("\n관리자 채팅이 {0} §r되었습니다.\n", user.isAdminChat() ? "§a§l활성화" : "§c§l비활성화");
    }
}


