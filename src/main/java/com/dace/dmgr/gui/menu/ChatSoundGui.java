package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.Gui;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.item.ButtonItem;
import com.dace.dmgr.gui.item.DisplayItem;
import com.dace.dmgr.lobby.ChatSound;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.util.SoundUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.dace.dmgr.system.HashMapList.userMap;

public class ChatSoundGui extends Gui {
    public ChatSoundGui(Player player) {
        super(2, "§8채팅 효과음 설정");
        super.fillRow(2, DisplayItem.EMPTY.getItemStack());

        User user = userMap.get(player);

        ChatSound[] chatSounds = ChatSound.values();

        for (int i = 0; i < chatSounds.length; i++) {
            super.setSelectButton(i, new ItemBuilder(chatSounds[i].getMaterial()).setName("§e§l" + chatSounds[i].getName()).build(),
                    user.getUserConfig().getChatSound() == chatSounds[i]);
        }

        super.getInventory().setItem(17, ButtonItem.LEFT.getItemStack());
    }

    @Override
    protected void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
            User user = userMap.get(player);

            if (clickItemName.equals("이전")) {
                player.performCommand("설정");
                return;
            }

            ChatSound chatSound = ChatSound.valueOf(clickItemName);

            SoundUtil.play(chatSound.getSound(), 1F, 1.414F, player);
            user.getUserConfig().setChatSound(chatSound);
            new ChatSoundGui(player).open(player);
        }
    }
}
