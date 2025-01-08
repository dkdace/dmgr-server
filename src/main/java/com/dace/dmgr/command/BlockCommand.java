package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.BlockList;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 차단 명령어 클래스.
 *
 * @see UserData#addBlockedPlayer(UserData)
 */
public final class BlockCommand extends CommandHandler {
    @Getter
    private static final BlockCommand instance = new BlockCommand();

    private BlockCommand() {
        super("차단", new ParameterList(ParameterType.PLAYER_NAME));

        new List();
        new Clear();
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(sender);

        if (args.length == 0) {
            user.sendMessageInfo(getFullHelp());
            return;
        }

        UserData targetUserData = UserData.fromPlayerName(args[0]);
        if (targetUserData == null) {
            sendWarnPlayerNotFound(sender);
            return;
        }

        UserData userData = user.getUserData();
        if (userData.isBlockedPlayer(targetUserData))
            userData.removeBlockedPlayer(targetUserData);
        else
            userData.addBlockedPlayer(targetUserData);

        user.sendMessageInfo("§e§n{0}§r님의 {1}.",
                targetUserData.getPlayerName(),
                userData.isBlockedPlayer(targetUserData) ? "채팅을 차단했습니다" : "채팅 차단을 해제했습니다");
    }

    private final class List extends Subcommand {
        private List() {
            super("(목록|list)", "차단 목록을 확인합니다.", "목록", "list");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            User user = User.fromPlayer(sender);

            if (user.getUserData().getBlockedPlayers().isEmpty()) {
                user.sendMessageWarn("차단한 플레이어가 없습니다.");
                return;
            }

            BlockList.getInstance().open(sender);
        }
    }

    private final class Clear extends Subcommand {
        private Clear() {
            super("(초기화|clear)", "차단 목록을 초기화합니다.", "초기화", "clear");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            User user = User.fromPlayer(sender);

            user.sendMessageInfo("차단 목록을 초기화했습니다.");
            user.getUserData().clearBlockedPlayers();
        }
    }
}
