package com.dace.dmgr.command;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 경고 명령어 클래스.
 *
 * @see UserData#setWarning(int)
 */
public final class WarningCommand extends CommandHandler {
    @Getter
    private static final WarningCommand instance = new WarningCommand();

    private WarningCommand() {
        super("경고");

        new Check();
        new Add();
        new Subtract();
        new Clear();
    }

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User.fromPlayer(player).sendMessageInfo(getFullHelp());
    }

    /**
     * 경고 확인 명령어 클래스.
     */
    private final class Check extends Subcommand {
        private Check() {
            super("(보기|check) [플레이어]", "자신 또는 대상 플레이어의 누적 경고를 봅니다.", new ParameterList(ParameterType.PLAYER_NAME),
                    "보기", "check");
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

            user.sendMessageInfo("§e§n{0}§r님의 누적 경고 횟수 : §c{1}회", targetUserData.getPlayerName(), targetUserData.getWarning());
        }
    }

    /**
     * 경고 주기 명령어 클래스.
     */
    private final class Add extends Subcommand {
        private Add() {
            super("(주기|add) <플레이어> [사유...]", "플레이어의 경고를 1회 추가합니다.", true,
                    new ParameterList(true, ParameterType.PLAYER_NAME, ParameterType.STRING), "주기", "add");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            User user = User.fromPlayer(sender);

            if (args.length == 0) {
                sendWarnWrongUsage(sender);
                return;
            }

            UserData targetUserData = UserData.fromPlayerName(args[0]);
            if (targetUserData == null) {
                sendWarnPlayerNotFound(sender);
                return;
            }

            targetUserData.setWarning(targetUserData.getWarning() + 1);

            String reason = args.length >= 2 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;

            User.getAllUsers().forEach(target -> {
                target.sendMessageInfo("\n§e§n{0}§r님이 §e§n{1}§r님에게 경고를 주었습니다.",
                        user.getPlayer().getName(),
                        targetUserData.getPlayerName());
                if (reason != null)
                    target.sendMessageInfo("경고 사유 : §6{0}", reason);
                target.sendMessageInfo("누적 경고 횟수 : §c{0}회\n", targetUserData.getWarning());
            });

            if (targetUserData.getWarning() >= 3) {
                new DelayTask(() -> {
                    Timestamp expiration = targetUserData.ban(Timespan.ofDays(Math.pow(3, targetUserData.getWarning() - 2.0)), reason);

                    User.getAllUsers().forEach(target ->
                            target.sendMessageInfo("\n" +
                                            "§e§n{0}§r님이 경고 §c3회 §f누적으로 서버에서 차단되었습니다.\n" +
                                            "차단 해제 일시 : §c§n{1}\n",
                                    targetUserData.getPlayerName(),
                                    DateFormatUtils.format(expiration.toDate(), "yyyy-MM-dd HH:mm:ss")));
                }, 20);
            }
        }
    }

    /**
     * 경고 빼기 명령어 클래스.
     */
    private final class Subtract extends Subcommand {
        private Subtract() {
            super("(빼기|sub[tract]) <플레이어>", "플레이어의 경고를 1회 차감합니다.", true,
                    new ParameterList(ParameterType.PLAYER_NAME), "빼기", "subtract", "sub");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            User user = User.fromPlayer(sender);

            if (args.length != 1) {
                sendWarnWrongUsage(sender);
                return;
            }

            UserData targetUserData = UserData.fromPlayerName(args[0]);
            if (targetUserData == null) {
                sendWarnPlayerNotFound(sender);
                return;
            }

            targetUserData.setWarning(Math.max(0, targetUserData.getWarning() - 1));

            User.getAllUsers().forEach(target ->
                    target.sendMessageInfo("\n" +
                                    "§e§n{0}§r님이 §e§n{1}§r님의 경고를 차감했습니다.\n" +
                                    "누적 경고 횟수 : §c{2}회\n",
                            user.getPlayer().getName(),
                            targetUserData.getPlayerName(),
                            targetUserData.getWarning()));
        }
    }

    /**
     * 경고 초기화 명령어 클래스.
     */
    private final class Clear extends Subcommand {
        private Clear() {
            super("(초기화|clear) <플레이어>", "플레이어의 경고를 초기화합니다.", true,
                    new ParameterList(ParameterType.PLAYER_NAME), "초기화", "clear");
        }

        @Override
        protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
            User user = User.fromPlayer(sender);

            if (args.length != 1) {
                sendWarnWrongUsage(sender);
                return;
            }

            UserData targetUserData = UserData.fromPlayerName(args[0]);
            if (targetUserData == null) {
                sendWarnPlayerNotFound(sender);
                return;
            }

            targetUserData.setWarning(0);

            User.getAllUsers().forEach(target ->
                    target.sendMessageInfo("\n§e§n{0}§r님이 §e§n{1}§r님의 경고를 초기화했습니다.\n",
                            user.getPlayer().getName(),
                            targetUserData.getPlayerName()));
        }
    }
}


