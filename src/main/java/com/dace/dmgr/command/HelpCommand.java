package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 명령어 목록 확인 명령어 클래스.
 */
public final class HelpCommand extends CommandHandler {
    /** 명령어 목록 표시 메시지 */
    private static final String MESSAGE_HELP = String.join("\n",
            StringFormUtil.BAR,
            MenuCommand.getInstance().getHelp(),
            DiscordCommand.getInstance().getHelp(),
            MinelistCommand.getInstance().getHelp(),
            LobbyCommand.getInstance().getHelp(),
            QuitCommand.getInstance().getHelp(),
            StatCommand.getInstance().getHelp(),
            DMCommand.getInstance().getHelp(),
            BlockCommand.getInstance().getHelp(),
            RankingCommand.getInstance().getHelp(),
            WarningCommand.getInstance().getHelp(),
            StringFormUtil.BAR);
    @Getter
    private static final HelpCommand instance = new HelpCommand();

    private HelpCommand() {
        super("명령어");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User.fromPlayer(sender).sendMessageInfo(MESSAGE_HELP);
    }
}
