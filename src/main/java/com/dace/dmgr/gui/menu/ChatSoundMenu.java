package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.Menu;
import com.dace.dmgr.gui.slot.ButtonSlot;
import com.dace.dmgr.gui.slot.DisplaySlot;
import com.dace.dmgr.user.ChatSound;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.userHashMap;

public class ChatSoundMenu extends Menu {
    public ChatSoundMenu(Player player) {
        super(2, "§8채팅 효과음 설정");
        super.fillRow(2, ItemBuilder.fromSlotItem(DisplaySlot.EMPTY).build());

        User user = userHashMap.get(player);
        UserConfig userConfig = user.getUserConfig();

        super.setSelectButton(0, new ItemBuilder(Material.BARRIER).setName("§c§l음소거").build(),
                userConfig.getChatSound() == ChatSound.MUTE);
        super.setSelectButton(1, new ItemBuilder(Material.GLOWSTONE).setName("§e§l플링 §f(기본값)").build(),
                userConfig.getChatSound() == ChatSound.PLING);
        super.setSelectButton(2, new ItemBuilder(Material.GRASS).setName("§e§l하프").build(),
                userConfig.getChatSound() == ChatSound.HARP);
        super.setSelectButton(3, new ItemBuilder(Material.WOOD).setName("§e§l더블 베이스").build(),
                userConfig.getChatSound() == ChatSound.DOUBLE_BASS);
        super.setSelectButton(4, new ItemBuilder(Material.WOOL).setName("§e§l기타").build(),
                userConfig.getChatSound() == ChatSound.GUITAR);
        super.setSelectButton(5, new ItemBuilder(Material.GOLD_BLOCK).setName("§e§l벨").build(),
                userConfig.getChatSound() == ChatSound.BELL);
        super.setSelectButton(6, new ItemBuilder(Material.PACKED_ICE).setName("§e§l차임벨").build(),
                userConfig.getChatSound() == ChatSound.CHIMEBELL);
        super.setSelectButton(7, new ItemBuilder(Material.SOUL_SAND).setName("§e§l카우벨").build(),
                userConfig.getChatSound() == ChatSound.COWBELL);
        super.setSelectButton(8, new ItemBuilder(Material.CLAY).setName("§e§l플룻").build(),
                userConfig.getChatSound() == ChatSound.FLUTE);
        super.setSelectButton(9, new ItemBuilder(Material.QUARTZ_BLOCK).setName("§e§l실로폰").build(),
                userConfig.getChatSound() == ChatSound.XYLOPHONE);
        super.setSelectButton(10, new ItemBuilder(Material.IRON_BLOCK).setName("§e§l철 실로폰").build(),
                userConfig.getChatSound() == ChatSound.IRON_XYLOPHONE);
        super.setSelectButton(11, new ItemBuilder(Material.PUMPKIN).setName("§e§l디저리두").build(),
                userConfig.getChatSound() == ChatSound.DIDGERIDOO);
        super.setSelectButton(12, new ItemBuilder(Material.EMERALD_BLOCK).setName("§e§l비트").build(),
                userConfig.getChatSound() == ChatSound.BIT);
        super.setSelectButton(13, new ItemBuilder(Material.HAY_BLOCK).setName("§e§l벤조").build(),
                userConfig.getChatSound() == ChatSound.BANJO);
        super.getGui().setItem(17, ItemBuilder.fromSlotItem(ButtonSlot.LEFT).build());
    }
}
