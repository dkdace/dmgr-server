package com.dace.dmgr.system.command.test;

import com.dace.dmgr.combat.entity.Dummy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 훈련용 봇 소환 명령어 클래스.
 *
 * <p>Usage: /소환 체력</p>
 */
public class DummyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        int health = Integer.parseInt(args[0]);

        new Dummy(player.getLocation(), health);

        return true;
    }
}
