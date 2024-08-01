package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.Stat;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 전적 명령어 클래스.
 *
 * <p>Usage: /전적 [플레이어]</p>
 *
 * @see Stat
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatCommand extends BaseCommandExecutor {
    @Getter
    private static final StatCommand instance = new StatCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        UserData targetUserData = user.getUserData();
        if (args.length == 1) {
            targetUserData = Arrays.stream(UserData.getAllUserDatas())
                    .filter(target -> target.getPlayerName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .orElse(null);

            if (targetUserData == null) {
                user.sendMessageWarn("플레이어를 찾을 수 없습니다.");
                return;
            }
        } else if (args.length > 1) {
            user.sendMessageWarn("올바른 사용법: §n'/(전적|stat) [플레이어]'");
            return;
        }

        Stat stat = new Stat(targetUserData);
        stat.open(player);
    }


    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length != 1)
            return null;

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}


