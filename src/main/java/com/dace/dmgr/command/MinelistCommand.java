package com.dace.dmgr.command;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 마인리스트 명령어 클래스.
 */
public final class MinelistCommand extends CommandHandler {
    @Getter
    private static final MinelistCommand instance = new MinelistCommand();

    private MinelistCommand() {
        super("마인리스트");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User.fromPlayer(sender).sendMessageInfo("\n§n" + GeneralConfig.getConfig().getMinelist() + "\n");
    }
}
