package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 귓속말 명령어 클래스.
 *
 * <p>Usage: /귓속말 [플레이어]</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DMCommand extends BaseCommandExecutor {
    @Getter
    private static final DMCommand instance = new DMCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                user.sendMessageWarn("플레이어를 찾을 수 없습니다.");
                return;
            }

            user.setMessageTarget(User.fromPlayer(target));
            user.sendMessageInfo("");
            user.sendMessageInfo("§e§n{0}§r님과의 대화가 시작되었습니다.", target.getName());
            user.sendMessageInfo("종료하려면 §n'/(귓[속말]|dm)'§r을 다시 입력하십시오.");
            user.sendMessageInfo("");
        } else {
            if (user.getMessageTarget() == null) {
                user.sendMessageWarn("올바른 사용법: §n'/(귓[속말]|dm) <플레이어>'");
                return;
            }

            user.setMessageTarget(null);
            user.sendMessageInfo("");
            user.sendMessageInfo("개인 대화가 종료되었습니다.");
            user.sendMessageInfo("");
        }
    }


    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length != 1)
            return null;

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}


