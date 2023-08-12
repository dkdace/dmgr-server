package com.dace.dmgr.system.command.test;

import com.kiwi.dmgr.game.mode.EnumGameMode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;
import static com.kiwi.dmgr.match.MatchMaking.addPlayerUnranked;

/**
 * 게임 테스트 명령어 클래스
 */
public class GameTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equals("추가")) {
            Player player = Bukkit.getServer().getPlayer(args[1]);
            if (player.isOnline())
                addPlayerUnranked(player, EnumGameMode.TeamDeathMatch);
        }

        if (args[0].equals("강제종료")) {
            if (gameUserMap.get(sender) != null) {
                gameUserMap.get(sender).getGame().finish(false);
            }
        }

        return true;
    }
}
