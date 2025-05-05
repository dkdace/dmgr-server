package com.dace.dmgr.command;

import com.dace.dmgr.menu.Stat;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 전적 명령어 클래스.
 *
 * @see Stat
 */
public final class StatCommand extends CommandHandler {
    @Getter
    private static final StatCommand instance = new StatCommand();

    private StatCommand() {
        super("전적", new ParameterList(ParameterType.PLAYER_NAME));
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);

        if (args.length > 1) {
            sendWarnWrongUsage(sender);
            return;
        }

        UserData targetUserData = user.getUserData();

        if (args.length == 1) {
            targetUserData = UserData.fromPlayerName(args[0]);
            if (targetUserData == null) {
                sendWarnPlayerNotFound(sender);
                return;
            }
        }

        new Stat(sender, targetUserData);
    }
}


