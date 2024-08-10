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
 * 게임 퇴장 명령어 클래스.
 *
 * <p>Usage: /퇴장</p>
 *
 * @see GameUser#dispose()
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuitCommand extends BaseCommandExecutor {
    @Getter
    private static final QuitCommand instance = new QuitCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser != null) {
            Game game = gameUser.getGame();
            if (game.getPhase() == Game.Phase.READY || game.getPhase() == Game.Phase.PLAYING)
                user.reset();

            gameUser.dispose();
        }
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        return null;
    }
}
