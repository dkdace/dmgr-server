package com.dace.dmgr.system.command.test;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.system.GameInfoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

/**
 * 게임 테스트 명령어 클래스.
 */
public class GameTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int number = Integer.parseInt(args[1]);

        switch (args[0]) {
            case "생성": {
                Game game = new Game(false, number);
                game.init();
                DMGR.getPlugin().getLogger().info(MessageFormat.format("일반 게임 생성됨 : [{0}]", number));

                break;
            }
            case "전체추가": {
                Game game = GameInfoRegistry.getGame(false, number);
                for (Player player : Bukkit.getOnlinePlayers())
                    game.addPlayer(player);

                break;
            }
            case "삭제": {
                Game game = GameInfoRegistry.getGame(false, number);
                game.remove();
                DMGR.getPlugin().getLogger().info(MessageFormat.format("일반 게임 제거됨 : [{0}]", number));

                break;
            }
        }

        return true;
    }
}
