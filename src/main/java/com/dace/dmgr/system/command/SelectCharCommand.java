package com.dace.dmgr.system.command;

import com.dace.dmgr.admin.Admin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SelectCharCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Admin.selectCharacter(Bukkit.getPlayer(args[0]), args[1], args[2]);

        return true;
    }
}
