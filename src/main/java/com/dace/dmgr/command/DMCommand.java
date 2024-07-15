package com.dace.dmgr.command;

import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 귓속말 명령어 클래스.
 *
 * <p>Usage: /귓속말 [플레이어]</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DMCommand implements CommandExecutor {
    @Getter
    private static final DMCommand instance = new DMCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        User user = User.fromPlayer(player);

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                user.sendMessageWarn("플레이어를 찾을 수 없습니다.");
                return true;
            }

            user.setMessageTarget(User.fromPlayer(target));
            user.sendMessageInfo("");
            user.sendMessageInfo("§e§n{0}§r님과의 대화가 시작되었습니다.", target.getName());
            user.sendMessageInfo("종료하려면 §n'/(귓[속말]|dm)'§r을 다시 입력하십시오.");
            user.sendMessageInfo("");
        } else {
            if (user.getMessageTarget() == null) {
                user.sendMessageWarn("올바른 사용법: §n'/(귓[속말]|dm) <플레이어>'");
                return true;
            }

            user.setMessageTarget(null);
            user.sendMessageInfo("");
            user.sendMessageInfo("개인 대화가 종료되었습니다.");
            user.sendMessageInfo("");
        }

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Tab implements TabCompleter {
        @Getter
        private static final Tab instance = new Tab();

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            List<String> completions = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            if (args.length == 1)
                return completions.stream().filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            return Collections.emptyList();
        }
    }
}


