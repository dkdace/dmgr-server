package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.Menu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 메뉴 명령어 클래스.
 *
 * <p>Usage: /메뉴</p>
 *
 * @see Menu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MenuCommand implements CommandExecutor {
    @Getter
    private static final MenuCommand instance = new MenuCommand();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        Menu menu = Menu.getInstance();
        menu.open(player);

        return true;
    }
}
