package com.dace.dmgr.command;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 전체/팀 채팅 명령어 클래스.
 *
 * <p>Usage: /채팅</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamChatCommand extends BaseCommandExecutor {
    @Getter
    private static final TeamChatCommand instance = new TeamChatCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);
        if (gameUser == null || (gameUser.getGame().getPhase() != Game.Phase.READY && gameUser.getGame().getPhase() != Game.Phase.PLAYING)) {
            user.sendMessageWarn("게임 진행 중에만 사용할 수 있습니다.");
            return;
        }

        if (gameUser.isTeamChat()) {
            user.sendMessageInfo("전체 채팅이 §a§l활성화 §r되었습니다.");
            gameUser.setTeamChat(false);
        } else {
            user.sendMessageInfo("전체 채팅이 §c§l비활성화 §r되었습니다.");
            gameUser.setTeamChat(true);
        }
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        return null;
    }
}
