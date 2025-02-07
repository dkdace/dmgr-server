package com.dace.dmgr.command;

import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 로비 이동 명령어 클래스.
 *
 * @see User#reset()
 */
public final class LobbyCommand extends CommandHandler {
    @Getter
    private static final LobbyCommand instance = new LobbyCommand();

    private LobbyCommand() {
        super("스폰");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser != null) {
            user.sendMessageWarn("게임 진행 중에 나가려면 §n'/quit'§r 또는 §n'/q'§r를 입력하십시오.");
            return;
        }

        user.reset();
    }
}
