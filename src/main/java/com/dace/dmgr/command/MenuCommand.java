package com.dace.dmgr.command;

import com.dace.dmgr.menu.Menu;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 메뉴 명령어 클래스.
 *
 * @see Menu
 */
public final class MenuCommand extends CommandHandler {
    @Getter
    private static final MenuCommand instance = new MenuCommand();

    private MenuCommand() {
        super("메뉴");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
        new Menu(sender);
    }
}
