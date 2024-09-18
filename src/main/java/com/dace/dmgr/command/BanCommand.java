package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 서버 차단(밴) 명령어 클래스.
 *
 * <p>Usage: /밴 <플레이어> [기간(일)] [사유...]</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BanCommand extends BaseCommandExecutor {
    @Getter
    private static final BanCommand instance = new BanCommand();

    /**
     * 밴 해제 명령어.
     *
     * @param player         입력자
     * @param targetUserData 대상 플레이어
     */
    private static void unban(@NonNull Player player, @NonNull UserData targetUserData) {
        targetUserData.unban();

        Bukkit.getOnlinePlayers().forEach(target -> {
            User targetUser = User.fromPlayer(target);
            targetUser.sendMessageInfo("");
            targetUser.sendMessageInfo("§e§n{0}§r님이 §e§n{1}§r님의 서버 차단을 해제했습니다.", player.getName(), targetUserData.getPlayerName());
            targetUser.sendMessageInfo("");
        });
    }

    /**
     * 밴 명령어.
     *
     * @param user           입력자
     * @param targetUserData 대상 플레이어
     * @param args           인수 목록
     */
    private static void ban(@NonNull User user, @NonNull UserData targetUserData, @NonNull String @NonNull [] args) {
        if (!StringUtils.isNumeric(args[1])) {
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(밴|ban) <플레이어> [기간(일)] [사유...]");
            return;
        }

        int days = Integer.parseInt(args[1]);
        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        Date endDate = targetUserData.ban(days, reason);

        Bukkit.getOnlinePlayers().forEach(target -> {
            User targetUser = User.fromPlayer(target);
            targetUser.sendMessageInfo("");
            targetUser.sendMessageInfo("§e§n{0}§r님이 §e§n{1}§r님을 서버에서 차단했습니다.", user.getPlayer().getName(), targetUserData.getPlayerName());
            if (reason != null)
                targetUser.sendMessageInfo("차단 사유 : §6{0}", reason);
            targetUser.sendMessageInfo("차단 해제 일시 : §c§n{0}", DateFormatUtils.format(endDate, "YYYY-MM-dd HH:mm:ss"));
            targetUser.sendMessageInfo("");
        });
    }

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        if (args.length >= 1) {
            UserData targetUserData = Arrays.stream(UserData.getAllUserDatas())
                    .filter(target -> target.getPlayerName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .orElse(null);

            if (targetUserData == null) {
                user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
                return;
            }

            if (args.length == 1) {
                if (targetUserData.isBanned())
                    unban(player, targetUserData);
                else
                    user.sendMessageWarn("해당 플레이어는 차단된 상태가 아닙니다.");
            } else
                ban(user, targetUserData, args);
        } else
            user.sendMessageWarn(WARN_WRONG_USAGE, "/(밴|ban) <플레이어> [기간(일)] [사유...]");
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length != 1)
            return null;

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}


