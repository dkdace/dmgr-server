package com.dace.dmgr.system.command;

import com.dace.dmgr.gui.menu.OptionMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OptionCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        OptionMenu optionMenu = new OptionMenu(player);
        optionMenu.open(player);

        return true;
    }
}
