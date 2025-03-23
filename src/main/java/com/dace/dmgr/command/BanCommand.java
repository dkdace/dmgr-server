package com.dace.dmgr.command;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * 서버 차단(밴) 명령어 클래스.
 *
 * @see UserData#ban(Timespan, String)
 */
public final class BanCommand extends CommandHandler {
    @Getter
    private static final BanCommand instance = new BanCommand();

    private BanCommand() {
        super("밴", new ParameterList(true, ParameterType.PLAYER_NAME, ParameterType.INTEGER, ParameterType.STRING));
    }

    /**
     * 밴 해제 명령어.
     *
     * @param sender         입력자
     * @param targetUserData 대상 플레이어
     */
    private void unban(@NonNull Player sender, @NonNull UserData targetUserData) {
        targetUserData.unban();

        Bukkit.getOnlinePlayers().forEach(target ->
                User.fromPlayer(target).sendMessageInfo("\n§e§n{0}§r님이 §e§n{1}§r님의 서버 차단을 해제했습니다.\n",
                        sender.getName(),
                        targetUserData.getPlayerName()));
    }

    /**
     * 밴 명령어.
     *
     * @param sender         입력자
     * @param targetUserData 대상 플레이어
     * @param args           인수 목록
     */
    private void ban(@NonNull Player sender, @NonNull UserData targetUserData, @NonNull String @NonNull [] args) {
        if (!StringUtils.isNumeric(args[1])) {
            sendWarnWrongUsage(sender);
            return;
        }

        int days = Integer.parseInt(args[1]);
        String reason = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : null;

        Timestamp expiration = targetUserData.ban(Timespan.ofDays(days), reason);

        User.getAllUsers().forEach(target -> {
            target.sendMessageInfo("\n§e§n{0}§r님이 §e§n{1}§r님을 서버에서 차단했습니다.",
                    sender.getPlayer().getName(),
                    targetUserData.getPlayerName());
            if (reason != null)
                target.sendMessageInfo("차단 사유 : §6{0}", reason);
            target.sendMessageInfo("차단 해제 일시 : §c§n{0}\n", DateFormatUtils.format(expiration.toDate(), "yyyy-MM-dd HH:mm:ss"));
        });
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

        if (args.length == 1) {
            if (targetUserData.isBanned())
                unban(sender, targetUserData);
            else
                user.sendMessageWarn("해당 플레이어는 차단된 상태가 아닙니다.");
        } else
            ban(sender, targetUserData, args);
    }
}


