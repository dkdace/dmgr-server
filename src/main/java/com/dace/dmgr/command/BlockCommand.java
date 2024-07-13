package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.BlockList;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.StringFormUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 차단 명령어 클래스.
 *
 * <p>Usage: /차단 (<플레이어>|목록|초기화)</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockCommand implements CommandExecutor {
    /** 도움말 메시지 */
    private static final String MESSAGE_HELP = StringFormUtil.BAR +
            "\n§a§l/(차단|block) <플레이어> - §a플레이어의 채팅을 차단하거나 차단 해제합니다." +
            "\n§a§l/(차단|block) (목록|list) - §a차단 목록을 확인합니다." +
            "\n§a§l/(차단|block) (초기화|clear) - §a차단 목록을 초기화합니다." +
            "\n" + StringFormUtil.BAR;
    @Getter
    private static final BlockCommand instance = new BlockCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        User user = User.fromPlayer(player);
        UserData userData = user.getUserData();

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "목록":
                case "ahrfhr":
                case "list": {
                    if (userData.getBlockedPlayers().length == 0) {
                        user.sendMessageWarn("차단한 플레이어가 없습니다.");
                        player.closeInventory();
                        return true;
                    }

                    BlockList blockList = BlockList.getInstance();
                    blockList.open(player);

                    break;
                }
                case "초기화":
                case "chrlghk":
                case "clear": {
                    user.sendMessageInfo("차단 목록을 초기화했습니다.");
                    userData.clearBlockedPlayers();

                    break;
                }
                default: {
                    UserData targetUserData = Arrays.stream(UserData.getAllUserDatas())
                            .filter(userData2 -> userData2.getPlayerName().equalsIgnoreCase(args[0]))
                            .findFirst()
                            .orElse(null);

                    if (targetUserData == null) {
                        user.sendMessageWarn("플레이어를 찾을 수 없습니다.");
                        return true;
                    }

                    if (userData.isBlockedPlayer(targetUserData)) {
                        user.sendMessageInfo("§e§n" + targetUserData.getPlayerName() + "§r님의 채팅 차단을 해제했습니다.");
                        userData.removeBlockedPlayer(targetUserData);
                    } else {
                        user.sendMessageInfo("§e§n" + targetUserData.getPlayerName() + "§r님의 채팅을 차단했습니다.");
                        userData.addBlockedPlayer(targetUserData);
                    }

                    break;
                }
            }
        } else
            user.sendMessageInfo(MESSAGE_HELP);

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Tab implements TabCompleter {
        @Getter
        private static final Tab instance = new Tab();

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            switch (alias.toLowerCase()) {
                case "차단":
                case "ckeks":
                    completions.addAll(Arrays.asList("목록", "초기화"));
                    break;
                case "block":
                    completions.addAll(Arrays.asList("list", "clear"));
                    break;
            }
            if (args.length == 1)
                return completions.stream().filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            return Collections.emptyList();
        }
    }
}


