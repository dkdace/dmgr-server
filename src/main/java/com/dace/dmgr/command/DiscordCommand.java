package com.dace.dmgr.command;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 디스코드 명령어 클래스.
 */
public final class DiscordCommand extends CommandHandler {
    @Getter
    private static final DiscordCommand instance = new DiscordCommand();

    private DiscordCommand() {
        super("디스코드");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User.fromPlayer(sender).sendMessageInfo("\n§n" + GeneralConfig.getConfig().getDiscord() + "\n");
    }
}
