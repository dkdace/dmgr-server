package com.dace.dmgr.command;

import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 팀 채팅 전환 명령어 클래스.
 *
 * @see GameUser#setTeamChat(boolean)
 */
public final class TeamChatCommand extends CommandHandler {
    @Getter
    private static final TeamChatCommand instance = new TeamChatCommand();

    private TeamChatCommand() {
        super("채팅");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser == null) {
            user.sendMessageWarn("게임 진행 중에만 사용할 수 있습니다.");
            return;
        }

        gameUser.setTeamChat(!gameUser.isTeamChat());
        user.sendMessageInfo("전체 채팅이 {0} §r되었습니다.", gameUser.isTeamChat() ? "§c§l비활성화" : "§a§l활성화");
    }
}
