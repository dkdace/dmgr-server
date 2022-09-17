package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.Menu;
import com.dace.dmgr.gui.SkullIcon;
import com.dace.dmgr.gui.slot.ButtonSlot;
import com.dace.dmgr.gui.slot.DisplaySlot;
import com.dace.dmgr.user.User;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.userHashMap;

public class OptionMenu extends Menu {
    public OptionMenu(Player player) {
        super(2, "§8설정");
        super.fillRow(2, ItemBuilder.fromSlotItem(DisplaySlot.EMPTY).build());

        User user = userHashMap.get(player);

        super.setToggleButton(0,
                ItemBuilder.fromSkullIcon(SkullIcon.KOREAN_CHAT).setName("§e§l한글 자동 변환").setLore("§f채팅 자동 한글 변환을 활성화합니다.").build(),
                user.getUserConfig().isKoreanChat(), 9);
        super.setToggleButton(1,
                ItemBuilder.fromSkullIcon(SkullIcon.NIGHT_VISION).setName("§e§l야간 투시").setLore("§f야간 투시를 활성화합니다.").build(),
                user.getUserConfig().isNightVision(), 10);
        super.getGui().setItem(2,
                ItemBuilder.fromSkullIcon(SkullIcon.CROSSHAIR).setName("§e§l조준선 설정").setLore("§f조준선을 변경합니다.").build());
        super.getGui().setItem(3,
                ItemBuilder.fromSkullIcon(SkullIcon.CHAT_SOUND).setName("§e§l채팅 효과음 설정").setLore("§f채팅 효과음을 변경하거나 끕니다.").build());
        super.getGui().setItem(16, ItemBuilder.fromSlotItem(ButtonSlot.LEFT).build());
        super.getGui().setItem(17, ItemBuilder.fromSlotItem(ButtonSlot.EXIT).build());
    }
}
