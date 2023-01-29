package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.Gui;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.PlayerSkull;
import com.dace.dmgr.gui.item.ButtonItem;
import com.dace.dmgr.gui.item.DisplayItem;
import com.dace.dmgr.lobby.User;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.HashMapList.userMap;

public class PlayerOption extends Gui {
    public PlayerOption(Player player) {
        super(2, "§8설정");
        super.fillRow(2, DisplayItem.EMPTY.getItemStack());

        User user = userMap.get(player);

        super.setToggleButton(0,
                ItemBuilder.fromPlayerSkull(PlayerSkull.KOREAN_CHAT)
                        .setName("§e§l한글 자동 변환")
                        .setLore("§f채팅 자동 한글 변환을 활성화합니다.")
                        .build(),
                user.getUserConfig().isKoreanChat(), 9);
        super.setToggleButton(1,
                ItemBuilder.fromPlayerSkull(PlayerSkull.NIGHT_VISION)
                        .setName("§e§l야간 투시")
                        .setLore("§f야간 투시를 활성화합니다.")
                        .build(),
                user.getUserConfig().isNightVision(), 10);
        super.getInventory().setItem(2,
                ItemBuilder.fromPlayerSkull(PlayerSkull.CROSSHAIR)
                        .setName("§e§l조준선 설정")
                        .setLore("§f조준선을 변경합니다.")
                        .build());
        super.getInventory().setItem(3,
                ItemBuilder.fromPlayerSkull(PlayerSkull.CHAT_SOUND)
                        .setName("§e§l채팅 효과음 설정")
                        .setLore("§f채팅 효과음을 변경하거나 끕니다.")
                        .build());
        super.getInventory().setItem(16, ButtonItem.LEFT.getItemStack());
        super.getInventory().setItem(17, ButtonItem.EXIT.getItemStack());
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            User user = userMap.get(player);

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
                    new ChatSoundOption(player).open(player);
                    return;
                case "이전":
                    player.performCommand("메뉴");
                    return;
                case "나가기":
                    player.closeInventory();
                    return;
            }

            new PlayerOption(player).open(player);
        }
    }
}
