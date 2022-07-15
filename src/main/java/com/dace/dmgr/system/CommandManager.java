package com.dace.dmgr.system;

import com.dace.dmgr.gui.menu.MainMenu;
import com.dace.dmgr.data.model.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.EntityList.userList;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        User user = userList.get(((Player) sender).getUniqueId());

        switch (label) {
            case "메뉴":
                MainMenu mainMenu = new MainMenu(user);
                mainMenu.open(user.player);
        }

        return true;
    }
}
