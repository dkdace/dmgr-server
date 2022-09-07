package com.dace.dmgr.gui.menu.event;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MainMenuEvent extends MenuEvent {
    private static final MainMenuEvent instance = new MainMenuEvent();

    private MainMenuEvent() {
        super("메뉴");
    }

    public static MainMenuEvent getInstance() {
        return instance;
    }

    @Override
    public void onMenuClick(InventoryClickEvent event, Player player, ItemStack clickItem, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            super.playClickSound(player);

            switch (clickItemName) {
                case "게임 시작":
                    player.performCommand("시작");
                    break;
                case "이동":
                    player.performCommand("이동");
                    break;
                case "전적":
                    player.performCommand("전적");
                    break;
                case "설정":
                    player.performCommand("설정");
                    break;
                case "업적":
                    player.performCommand("업적");
                    break;
                case "명령어 목록":
                    player.performCommand("명령어");
                    break;
                case "차단 목록":
                    player.performCommand("차단 목록");
                    break;
                case "나가기":
                    player.closeInventory();
                    break;
            }
        }
    }
}
