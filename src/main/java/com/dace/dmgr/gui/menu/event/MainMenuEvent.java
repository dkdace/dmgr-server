package com.dace.dmgr.gui.menu.event;

import com.dace.dmgr.user.User;
import com.dace.dmgr.gui.menu.MenuEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.dace.dmgr.system.EntityList.userList;

public class MainMenuEvent extends MenuEvent {
    private static final MainMenuEvent instance = new MainMenuEvent();

    private MainMenuEvent() {
        super("메뉴");
    }

    public static MainMenuEvent getInstance() {
        return instance;
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player, ItemStack clickItem, String clickItemName) {
        User user = userList.get(player.getUniqueId());

        if (event.getClick() == ClickType.LEFT) {
            super.playClickSound(player);

            switch (clickItemName) {
                case "게임 시작":
                    player.performCommand("시작");
                    break;
                case "이동":
                    player.performCommand("이동");
                    break;
                case "나가기":
                    player.closeInventory();
                    break;
            }
        }
    }
}
