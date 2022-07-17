package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.ItemGenerator;
import com.dace.dmgr.gui.Menu;
import com.dace.dmgr.gui.slot.ButtonSlot;
import com.dace.dmgr.gui.slot.DisplaySlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainMenu extends Menu {
    public MainMenu(Player player) {
        super(6, "§8메뉴");
        super.fill(ItemGenerator.getSlotItem(DisplaySlot.EMPTY));

        super.getGui().setItem(4, ItemGenerator.getPlayerSkull(player, "§f§l[ null ] §f" + player.getName()));
        super.getGui().setItem(19,
                ItemGenerator.getItem(Material.IRON_SWORD, "§e§l게임 시작", "§f전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다."));
        super.getGui().setItem(21, ItemGenerator.getItem(Material.LEATHER_BOOTS, "§e§l이동", "§f원하는 장소로 이동합니다."));
        super.getGui().setItem(23, ItemGenerator.getItem(Material.NAME_TAG, "§e§l전적", "§f개인 전적을 확인합니다."));
        super.getGui().setItem(25, ItemGenerator.getItem(Material.BOOK, "§e§l업적", "§f업적 목록을 확인합니다."));
        super.getGui().setItem(37, ItemGenerator.getItem(Material.REDSTONE_COMPARATOR, "§e§l설정", "§f설정 관련 메뉴를 확인합니다."));
        super.getGui().setItem(39, ItemGenerator.getItem(Material.FIREWORK_CHARGE, "§e§l코어 확인", "§f전투원에 할당된 코어를 확인합니다."));
        super.getGui().setItem(41, ItemGenerator.getItem(Material.COMMAND, "§e§l명령어 목록", "§f서버 명령어 목록을 확인합니다."));
        super.getGui().setItem(43, ItemGenerator.getItem(Material.BARRIER, "§e§l차단 목록", "§f차단된 플레이어 목록을 확인합니다."));
        super.getGui().setItem(53, ItemGenerator.getSlotItem(ButtonSlot.EXIT));
    }
}
