package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.Menu;
import com.dace.dmgr.gui.slot.ButtonSlot;
import com.dace.dmgr.gui.slot.DisplaySlot;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MainMenu extends Menu {
    public MainMenu(Player player) {
        super(6, "§8메뉴");
        super.fill(ItemBuilder.fromSlotItem(DisplaySlot.EMPTY).build());

        super.getGui().setItem(4, ItemBuilder.fromPlayerSkull(player).setName("§f§l[ null ] §f" + player.getName()).build());
        super.getGui().setItem(19,
                new ItemBuilder(Material.IRON_SWORD).setName("§e§l게임 시작").setLore("§f전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.").build());
        super.getGui().setItem(21,
                new ItemBuilder(Material.LEATHER_BOOTS).setName("§e§l이동").setLore("§f원하는 장소로 이동합니다.").build());
        super.getGui().setItem(23,
                new ItemBuilder(Material.NAME_TAG).setName("§e§l전적").setLore("§f개인 전적을 확인합니다.").build());
        super.getGui().setItem(25,
                new ItemBuilder(Material.BOOK).setName("§e§l업적").setLore("§f업적 목록을 확인합니다.").build());
        super.getGui().setItem(37,
                new ItemBuilder(Material.REDSTONE_COMPARATOR).setName("§e§l설정").setLore("§f설정 관련 메뉴를 확인합니다.").build());
        super.getGui().setItem(39,
                new ItemBuilder(Material.FIREWORK_CHARGE).setName("§e§l코어 확인").setLore("§f전투원에 할당된 코어를 확인합니다.").build());
        super.getGui().setItem(41,
                new ItemBuilder(Material.COMMAND).setName("§e§l명령어 목록").setLore("§f서버 명령어 목록을 확인합니다.").build());
        super.getGui().setItem(43,
                new ItemBuilder(Material.BARRIER).setName("§e§l차단 목록").setLore("§f차단된 플레이어 목록을 확인합니다.").build());
        super.getGui().setItem(53, ItemBuilder.fromSlotItem(ButtonSlot.EXIT).build());
    }
}
