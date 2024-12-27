package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.BlockList;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
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
 * 차단 명령어 클래스.
 *
 * <p>Usage: /차단 (<플레이어>|목록|초기화)</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockCommand extends BaseCommandExecutor {
    /** 도움말 메시지 */
    private static final String MESSAGE_HELP = StringFormUtil.BAR +
            "\n§a§l/(차단|block) <플레이어> - §a플레이어의 채팅을 차단하거나 차단 해제합니다." +
            "\n§a§l/(차단|block) (목록|list) - §a차단 목록을 확인합니다." +
            "\n§a§l/(차단|block) (초기화|clear) - §a차단 목록을 초기화합니다." +
            "\n" + StringFormUtil.BAR;
    @Getter
    private static final BlockCommand instance = new BlockCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        User user = User.fromPlayer(player);
        UserData userData = user.getUserData();

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "목록":
                case "ahrfhr":
                case "list": {
                    if (userData.getBlockedPlayers().isEmpty()) {
                        user.sendMessageWarn("차단한 플레이어가 없습니다.");
                        player.closeInventory();

                        return;
                    }

                    BlockList.getInstance().open(player);

                    return;
                }
                case "초기화":
                case "chrlghk":
                case "clear": {
                    user.sendMessageInfo("차단 목록을 초기화했습니다.");
                    userData.clearBlockedPlayers();

                    return;
                }
                default: {
                    UserData targetUserData = UserData.getAllUserDatas().stream()
                            .filter(target -> target.getPlayerName().equalsIgnoreCase(args[0]))
                            .findFirst()
                            .orElse(null);

                    if (targetUserData == null) {
                        user.sendMessageWarn(WARN_PLAYER_NOT_FOUND);
                        return;
                    }

                    if (userData.isBlockedPlayer(targetUserData)) {
                        user.sendMessageInfo("§e§n{0}§r님의 채팅 차단을 해제했습니다.", targetUserData.getPlayerName());
                        userData.removeBlockedPlayer(targetUserData);
                    } else {
                        user.sendMessageInfo("§e§n{0}§r님의 채팅을 차단했습니다.", targetUserData.getPlayerName());
                        userData.addBlockedPlayer(targetUserData);
                    }
                }
            }
        } else
            user.sendMessageInfo(MESSAGE_HELP);
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        if (args.length != 1)
            return null;

        List<String> completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        switch (alias.toLowerCase()) {
            case "차단":
            case "ckeks":
                completions.addAll(Arrays.asList("목록", "초기화"));
                break;
            case "block":
                completions.addAll(Arrays.asList("list", "clear"));
                break;
            default:
                break;
        }

        return completions;
    }
}


