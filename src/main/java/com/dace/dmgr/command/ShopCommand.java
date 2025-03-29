package com.dace.dmgr.command;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

/**
 * 상점 명령어 클래스.
 */
public final class ShopCommand extends CommandHandler {
    @Getter
    private static final ShopCommand instance = new ShopCommand();

    private ShopCommand() {
        super("상점");
    }

    @Override
    protected void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args) {
//        new ShopCore(sender);
    }
}
