package com.dace.dmgr.system.command.test;

import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 경험치 설정 명령어 클래스.
 */
public class XpTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        int xp = Integer.parseInt(args[0]);

        User user = EntityInfoRegistry.getUser(player);
        if (user == null)
            return true;

        user.getUserData().setXp(xp);

        return true;
    }
}
