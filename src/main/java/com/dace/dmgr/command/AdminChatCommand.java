package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 관리자 채팅 명령어 클래스.
 *
 * <p>Usage: /관라자채팅</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdminChatCommand extends BaseCommandExecutor {
    @Getter
    private static final AdminChatCommand instance = new AdminChatCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);

        user.sendMessageInfo("");
        if (user.isAdminChat()) {
            user.setAdminChat(false);
            user.sendMessageInfo("관리자 채팅이 §c비활성화 §f되었습니다.");
        } else {
            user.setAdminChat(true);
            user.sendMessageInfo("관리자 채팅이 §a활성화 §f되었습니다.");
        }
        user.sendMessageInfo("");
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        return null;
    }
}


