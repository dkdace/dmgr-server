package com.dace.dmgr.gui.menu.event;

import com.dace.dmgr.data.model.User;
import com.dace.dmgr.gui.menu.MenuEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MainMenuEvent extends MenuEvent {
    public MainMenuEvent() {
        super("메뉴");
    }

    @Override
    public void onClick(InventoryClickEvent event, User user, ItemStack clickItem, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            super.playClickSound(user);

            switch (clickItemName) {
                case "게임 시작":
                    user.player.performCommand("시작");
                    break;
                case "이동":
                    user.player.performCommand("이동");
                    break;
                case "나가기":
                    user.player.closeInventory();
                    break;
            }
        }
    }
}
