package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.Stat;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
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
 * 전적 명령어 클래스.
 *
 * <p>Usage: /전적 [플레이어]</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StatCommand implements CommandExecutor {
    @Getter
    private static final StatCommand instance = new StatCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        User user = User.fromPlayer(player);

        UserData targetUserData = user.getUserData();
        if (args.length == 1) {
            targetUserData = Arrays.stream(UserData.getAllUserDatas())
                    .filter(userData -> userData.getPlayerName().equalsIgnoreCase(args[0]))
                    .findFirst()
                    .orElse(null);

            if (targetUserData == null) {
                user.sendMessageWarn("플레이어를 찾을 수 없습니다.");
                return true;
            }
        } else if (args.length > 1) {
            user.sendMessageWarn("올바른 사용법: §n'/(전적|stat) [플레이어]'");
            return true;
        }

        Stat stat = new Stat(targetUserData);
        stat.open(player);

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Tab implements TabCompleter {
        @Getter
        private static final Tab instance = new Tab();

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            String[] completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new);
            if (args.length == 1)
                return Arrays.stream(completions).filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            return Collections.emptyList();
        }
    }
}


