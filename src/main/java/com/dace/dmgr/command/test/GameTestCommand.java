package com.dace.dmgr.command.test;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 게임 테스트 명령어 클래스.
 */
public class GameTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int number = Integer.parseInt(args[1]);

        switch (args[0]) {
            case "생성": {
                new Game(false, number);
                ConsoleLogger.info("일반 게임 생성됨 : [{0}]", number);

                break;
            }
            case "전체추가": {
                Game game = Game.fromNumber(false, number);
                for (Player player : Bukkit.getOnlinePlayers())
                    new GameUser(User.fromPlayer(player), game);

                break;
            }
            case "삭제": {
                Game game = Game.fromNumber(false, number);
                game.dispose();
                ConsoleLogger.info("일반 게임 제거됨 : [{0}]", number);

                break;
            }
        }

        return true;
    }
}
