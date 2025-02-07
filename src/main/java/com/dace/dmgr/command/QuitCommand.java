package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 게임 퇴장 명령어 클래스.
 */
public final class QuitCommand extends CommandHandler {
    @Getter
    private static final QuitCommand instance = new QuitCommand();

    private QuitCommand() {
        super("퇴장");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User.fromPlayer(sender).quitGame();
    }
}
