package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.Gui;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.item.ButtonItem;
import com.dace.dmgr.gui.item.DisplayItem;
import com.dace.dmgr.lobby.ChatSound;
import com.dace.dmgr.lobby.UserData;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.util.InventoryUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

/**
 * 메뉴 - 채팅 효과음 설정 GUI 클래스.
 */
public final class ChatSoundOption extends Gui {
    @Getter
    private static final ChatSoundOption instance = new ChatSoundOption();

    public ChatSoundOption() {
        super(2, "§8채팅 효과음 설정");
    }

    @Override
    public void onOpen(Player player, Inventory inventory) {
        UserData userData = EntityInfoRegistry.getUser(player).getUserData();

        ChatSound[] chatSounds = ChatSound.values();

        InventoryUtil.fillRow(inventory, 2, DisplayItem.EMPTY.getItemStack());
        for (int i = 0; i < chatSounds.length; i++) {
            InventoryUtil.setSelectButton(inventory, i, new ItemBuilder(chatSounds[i].getMaterial())
                            .setName("§e§l" + chatSounds[i].getName() + " " + "§8§o" + chatSounds[i].toString())
                            .build(),
                    userData.getUserConfig().getChatSound() == chatSounds[i]);
        }

        inventory.setItem(17, ButtonItem.LEFT.getItemStack());
    }

    @Override
    public void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            UserData userData = EntityInfoRegistry.getUser(player).getUserData();

            if (clickItemName.equals("이전")) {
                player.performCommand("설정");
                return;
            }

            String[] splittedClickItemName = clickItemName.split(" ");
            ChatSound chatSound = ChatSound.valueOf(splittedClickItemName[splittedClickItemName.length - 1]);

            SoundUtil.play(chatSound.getSound(), 1F, 1.414F, player);
            userData.getUserConfig().setChatSound(chatSound);
            open(player);
        }
    }
}
