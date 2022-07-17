package com.dace.dmgr.system.command;

import com.dace.dmgr.gui.menu.MainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainMenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        MainMenu mainMenu = new MainMenu(player);
        mainMenu.open(player);

        return true;
    }
}
