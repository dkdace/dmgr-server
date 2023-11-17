package com.dace.dmgr.system.command;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.util.BossBarUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 게임 퇴장 명령어 클래스.
 *
 * <p>Usage: /퇴장</p>
 *
 * @see Game#removePlayer(Player)
 */
public class QuitCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        GameUser gameUser = EntityInfoRegistry.getGameUser(player);
        if (gameUser != null) {
            gameUser.getGame().removePlayer(player);
            BossBarUtil.clearBossBar(player);
        }

        return true;
    }
}
