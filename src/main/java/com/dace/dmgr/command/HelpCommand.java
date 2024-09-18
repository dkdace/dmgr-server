package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 명령어 목록 확인 명령어 클래스.
 *
 * <p>Usage: /명령어</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HelpCommand extends BaseCommandExecutor {
    /** 명령어 목록 표시 메시지 */
    private static final String MESSAGE_HELP = StringFormUtil.BAR +
            "\n§a§l/(메뉴|menu) - §a메뉴 창을 엽니다. §nF키§a를 눌러 사용할 수도 있습니다." +
            "\n§a§l/(스폰|spawn|exit) - §a스폰(로비)으로 이동합니다." +
            "\n§a§l/(퇴장|quit) - §a현재 입장한 게임에서 나갑니다." +
            "\n§a§l/(전적|stat) [플레이어] - §a자신 또는 대상 플레이어의 개인 전적을 확인합니다." +
            "\n§a§l/(귓[속말]|dm) <플레이어> - §a대상 플레이어와의 개인 대화를 시작합니다." +
            "\n§a§l/(차단|block) - §a차단 관련 명령어를 확인합니다." +
            "\n§a§l/(랭킹|rank[ing]) - §a랭킹 관련 명령어를 확인합니다." +
            "\n§a§l/(경고|warn[ing]) - §a경고 관련 명령어를 확인합니다." +
            "\n" + StringFormUtil.BAR;
    @Getter
    private static final HelpCommand instance = new HelpCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User.fromPlayer(player).sendMessageInfo(MESSAGE_HELP);
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        return null;
    }
}
