package com.dace.dmgr.system.command;

import com.dace.dmgr.gui.menu.Menu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 메뉴 명령어 클래스.
 *
 * <p>Usage: /메뉴</p>
 */
public class MenuCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        Menu menu = new Menu(player);
        menu.open(player);

        return true;
    }
}
