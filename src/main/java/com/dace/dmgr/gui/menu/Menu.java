package com.dace.dmgr.gui.menu;

import com.dace.dmgr.gui.Gui;
import com.dace.dmgr.gui.ItemBuilder;
import com.dace.dmgr.gui.item.ButtonItem;
import com.dace.dmgr.gui.item.DisplayItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

public class Menu extends Gui {
    public Menu(Player player) {
        super(6, "§8메뉴");
        super.fillAll(DisplayItem.EMPTY.getItemStack());

        super.getInventory().setItem(4,
                ItemBuilder.fromPlayerSkull(player)
                        .setName("§f§l[ null ] §f" + player.getName())
                        .build());
        super.getInventory().setItem(19,
                new ItemBuilder(Material.IRON_SWORD)
                        .setName("§e§l게임 시작")
                        .setLore("§f전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.")
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
        super.getInventory().setItem(21,
                new ItemBuilder(Material.LEATHER_BOOTS)
                        .setName("§e§l이동")
                        .setLore("§f원하는 장소로 이동합니다.")
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
        super.getInventory().setItem(23,
                new ItemBuilder(Material.NAME_TAG)
                        .setName("§e§l전적")
                        .setLore("§f개인 전적을 확인합니다.")
                        .build());
        super.getInventory().setItem(25,
                new ItemBuilder(Material.BOOK)
                        .setName("§e§l업적")
                        .setLore("§f업적 목록을 확인합니다.")
                        .build());
        super.getInventory().setItem(37,
                new ItemBuilder(Material.REDSTONE_COMPARATOR)
                        .setName("§e§l설정")
                        .setLore("§f설정 관련 메뉴를 확인합니다.")
                        .build());
        super.getInventory().setItem(39,
                new ItemBuilder(Material.FIREWORK_CHARGE).setName("§e§l코어 확인")
                        .setLore("§f전투원에 할당된 코어를 확인합니다.")
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES).build());
        super.getInventory().setItem(41,
                new ItemBuilder(Material.COMMAND)
                        .setName("§e§l명령어 목록")
                        .setLore("§f서버 명령어 목록을 확인합니다.").build());
        super.getInventory().setItem(43,
                new ItemBuilder(Material.BARRIER)
                        .setName("§e§l차단 목록")
                        .setLore("§f차단된 플레이어 목록을 확인합니다.")
                        .build());
        super.getInventory().setItem(53, ButtonItem.EXIT.getItemStack());
    }

    @Override
    protected void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (event.getClick() == ClickType.LEFT) {
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
