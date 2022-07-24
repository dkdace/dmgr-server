package com.dace.dmgr.system.command;

import com.dace.dmgr.util.Admin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SelectCharCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        Admin.selectCharacter(player, args[0], args[1]);

        return true;
    }
}
