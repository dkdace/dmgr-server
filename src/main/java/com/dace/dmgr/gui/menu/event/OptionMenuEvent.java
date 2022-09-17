package com.dace.dmgr.gui.menu.event;

import com.dace.dmgr.gui.menu.ChatSoundMenu;
import com.dace.dmgr.gui.menu.OptionMenu;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.dace.dmgr.system.HashMapList.userHashMap;

public class OptionMenuEvent extends MenuEvent {
    private static final OptionMenuEvent instance = new OptionMenuEvent();

    private OptionMenuEvent() {
        super("설정");
    }

    public static OptionMenuEvent getInstance() {
        return instance;
    }

    @Override
    public void onMenuClick(InventoryClickEvent event, Player player, ItemStack clickItem, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            super.playClickSound(player);

            User user = userHashMap.get(player);

            switch (clickItemName) {
                case "한글 자동 변환":
                    user.getUserConfig().setKoreanChat(!user.getUserConfig().isKoreanChat());
                    if (user.getUserConfig().isKoreanChat())
                        player.performCommand("kakc chmod 2");
                    else
                        player.performCommand("kakc chmod 0");

                    break;
                case "야간 투시":
                    user.getUserConfig().setNightVision(!user.getUserConfig().isNightVision());
                    break;
                case "채팅 효과음 설정":
                    new ChatSoundMenu(player).open(player);
                    return;
                case "이전":
                    player.performCommand("메뉴");
                    return;
                case "나가기":
                    player.closeInventory();
                    return;
            }

            new OptionMenu(player).open(player);
        }
    }
}
