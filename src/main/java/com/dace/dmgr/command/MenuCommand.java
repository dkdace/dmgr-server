package com.dace.dmgr.command;

import com.dace.dmgr.item.gui.Menu;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 메뉴 명령어 클래스.
 *
 * <p>Usage: /메뉴</p>
 *
 * @see Menu
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MenuCommand extends BaseCommandExecutor {
    @Getter
    private static final MenuCommand instance = new MenuCommand();

    @Override
    protected void onCommandInput(@NonNull Player player, @NonNull String @NonNull [] args) {
        Menu menu = Menu.getInstance();
        menu.open(player);
    }

    @Override
    @Nullable
    protected List<@NonNull String> getCompletions(@NonNull String alias, @NonNull String @NonNull [] args) {
        return null;
    }
}
