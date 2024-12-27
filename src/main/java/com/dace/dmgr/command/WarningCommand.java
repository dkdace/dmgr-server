package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.Timespan;
import com.dace.dmgr.util.Timestamp;
import com.dace.dmgr.util.task.DelayTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 경고 명령어 클래스.
 *
 * <p>Usage: /경고 (보기|주기|빼기|초기화) <플레이어></p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WarningCommand extends BaseCommandExecutor {
    /** 도움말 메시지 */
    private static final String MESSAGE_HELP = StringFormUtil.BAR +
            "\n§a§l/(경고|warn[ing]) (보기|check) [플레이어] - §a자신 또는 대상 플레이어의 누적 경고를 봅니다." +
            "\n§a§l/(경고|warn[ing]) (주기|add) <플레이어> [사유...] - §a플레이어의 경고를 1회 증가합니다." +
            "\n§a§l/(경고|warn[ing]) (빼기|sub[tract]) <플레이어> - §a플레이어의 경고를 1회 차감합니다." +
            "\n§a§l/(경고|warn[ing]) (초기화|clear) <플레이어> - §a플레이어의 경고를 초기화합니다." +
            "\n" + StringFormUtil.BAR;
    @Getter
    private static final WarningCommand instance = new WarningCommand();

    /**
     * 경고 확인 명령어.
     *
     * @param user 입력자
     * @param args 인수 목록
     */
    private static void check(@NonNull User user, @NonNull String @NonNull [] args) {
        if (args.length > 2) {
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(경고|warn[ing]) (보기|check) [플레이어]");
            return;
        }

        UserData targetUserData = user.getUserData();
        if (args.length == 2) {
            targetUserData = UserData.getAllUserDatas().stream()
                    .filter(target -> target.getPlayerName().equalsIgnoreCase(args[1]))
                    .findFirst()
                    .orElse(null);

            if (targetUserData == null) {
                user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
                return;
            }
        }

        user.sendMessageInfo("§e§n{0}§r님의 누적 경고 횟수 : §c{1}회", targetUserData.getPlayerName(), targetUserData.getWarning());
    }

    /**
     * 경고 주기 명령어.
     *
     * @param user 입력자
     * @param args 인수 목록
     */
    private static void add(@NonNull User user, @NonNull String @NonNull [] args) {
        if (!user.getPlayer().isOp()) {
            user.sendMessageWarn(WARN_NO_PERMISSION);
            return;
        }
        if (args.length == 1) {
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(경고|warn[ing]) (주기|add) <플레이어> [사유...]");
            return;
        }

        UserData targetUserData = UserData.getAllUserDatas().stream()
                .filter(target -> target.getPlayerName().equalsIgnoreCase(args[1]))
                .findFirst()
                .orElse(null);

        if (targetUserData == null) {
            user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
            return;
        }

        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        targetUserData.setWarning(targetUserData.getWarning() + 1);
        Bukkit.getOnlinePlayers().forEach(target -> {
            User targetUser = User.fromPlayer(target);
            targetUser.sendMessageInfo("");
            targetUser.sendMessageInfo("§e§n{0}§r님이 §e§n{1}§r님에게 경고를 주었습니다.", user.getPlayer().getName(), targetUserData.getPlayerName());
            if (reason != null)
                targetUser.sendMessageInfo("경고 사유 : §6{0}", reason);
            targetUser.sendMessageInfo("누적 경고 횟수 : §c{0}회", targetUserData.getWarning());
            targetUser.sendMessageInfo("");
        });

        if (targetUserData.getWarning() >= 3) {
            new DelayTask(() -> {
                Timestamp expiration = targetUserData.ban(Timespan.ofDays(Math.pow(3, targetUserData.getWarning() - 2)), reason);

                Bukkit.getOnlinePlayers().forEach(target -> {
                    User targetUser = User.fromPlayer(target);
                    targetUser.sendMessageInfo("");
                    targetUser.sendMessageInfo("§e§n{0}§r님이 경고 §c3회 §f누적으로 서버에서 차단되었습니다.", targetUserData.getPlayerName());
                    targetUser.sendMessageInfo("차단 해제 일시 : §c§n{0}", DateFormatUtils.format(expiration.toDate(), "yyyy-MM-dd HH:mm:ss"));
                    targetUser.sendMessageInfo("");
                });
            }, 20);
        }
    }

    /**
     * 경고 빼기 명령어.
     *
     * @param user 입력자
     * @param args 인수 목록
     */
    private static void subtract(@NonNull User user, @NonNull String @NonNull [] args) {
        if (!user.getPlayer().isOp()) {
            user.sendMessageWarn(WARN_NO_PERMISSION);
            return;
        }
        if (args.length == 1 || args.length > 2) {
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(경고|warn[ing]) (빼기|add) <플레이어>");
            return;
        }

        UserData targetUserData = UserData.getAllUserDatas().stream()
                .filter(target -> target.getPlayerName().equalsIgnoreCase(args[1]))
                .findFirst()
                .orElse(null);

        if (targetUserData == null) {
            user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
            return;
        }

        targetUserData.setWarning(targetUserData.getWarning() - 1);
        Bukkit.getOnlinePlayers().forEach(target -> {
            User targetUser = User.fromPlayer(target);
            targetUser.sendMessageInfo("");
            targetUser.sendMessageInfo("§e§n{0}§r님이 §e§n{1}§r님의 경고를 차감했습니다.", user.getPlayer().getName(), targetUserData.getPlayerName());
            targetUser.sendMessageInfo("누적 경고 횟수 : §c{0}회", targetUserData.getWarning());
            targetUser.sendMessageInfo("");
        });
    }

    /**
     * 경고 초기화 명령어.
     *
     * @param user 입력자
     * @param args 인수 목록
     */
    private static void clear(@NonNull User user, @NonNull String @NonNull [] args) {
        if (!user.getPlayer().isOp()) {
            user.sendMessageWarn(WARN_NO_PERMISSION);
            return;
        }
        if (args.length == 1 || args.length > 2) {
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(경고|warn[ing]) (초기화|clear) <플레이어>");
            return;
        }

        UserData targetUserData = UserData.getAllUserDatas().stream()
                .filter(target -> target.getPlayerName().equalsIgnoreCase(args[1]))
                .findFirst()
                .orElse(null);

        if (targetUserData == null) {
            user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
            return;
        }

        targetUserData.setWarning(0);
        Bukkit.getOnlinePlayers().forEach(target -> {
            User targetUser = User.fromPlayer(target);
            targetUser.sendMessageInfo("");
            targetUser.sendMessageInfo("§e§n{0}§r님이 §e§n{1}§r님의 경고를 초기화했습니다.", user.getPlayer().getName(), targetUserData.getPlayerName());
            targetUser.sendMessageInfo("");
        });
    }

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "보기":
                case "qhrl":
                case "check": {
                    check(user, args);
                    return;
                }
                case "주기":
                case "wnrl":
                case "add": {
                    add(user, args);
                    return;
                }
                case "빼기":
                case "qorl":
                case "subtract":
                case "sub": {
                    subtract(user, args);
                    return;
                }
                case "초기화":
                case "chrlghk":
                case "clear": {
                    clear(user, args);
                    return;
                }
                default:
                    user.sendMessageInfo(MESSAGE_HELP);
            }
        } else
            user.sendMessageInfo(MESSAGE_HELP);
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length == 1)
            switch (alias.toLowerCase()) {
                case "경고":
                case "rudrh":
                    return Arrays.asList("보기", "주기", "빼기", "초기화");
                case "warn":
                case "warning":
                    return Arrays.asList("check", "add", "subtract", "clear");
                default:
                    break;
            }
        else if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        return null;
    }
}


