package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.function.Consumer;

/**
 * 메뉴 GUI 클래스.
 */
public final class Menu extends Gui {
    @Getter
    private static final Menu instance = new Menu();
    private static final StaticItem playerInto = new StaticItem("MenuPlayer", new ItemBuilder(Material.SKULL_ITEM)
            .setDamage((short) 3)
            .build());

    public Menu() {
        super(6, "§8메뉴");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getStaticItem());
        guiController.set(4, playerInto, itemBuilder -> {
            UserData userData = UserData.fromPlayer(player);
            ((SkullMeta) itemBuilder.setName(userData.getDisplayName()).getItemMeta()).setOwningPlayer(player);
        });
        guiController.set(19, MenuItem.GAME_START.guiItem);
        guiController.set(21, MenuItem.WARP.guiItem);
        guiController.set(23, MenuItem.RECORD.guiItem);
        guiController.set(25, MenuItem.ACHIEVEMENT.guiItem);
        guiController.set(37, MenuItem.OPTION.guiItem);
        guiController.set(39, MenuItem.CORE.guiItem);
        guiController.set(41, MenuItem.COMMAND.guiItem);
        guiController.set(43, MenuItem.CHAT_BLOCK.guiItem);
        guiController.set(53, MenuItem.EXIT.guiItem);
    }

    @AllArgsConstructor
    private enum MenuItem {
        GAME_START(Material.IRON_SWORD, "게임 시작", "전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.",
                player -> SelectGame.getInstance().open(player)),
        WARP(Material.LEATHER_BOOTS, "이동", "원하는 장소로 이동합니다.",
                player -> Warp.getInstance().open(player)),
        RECORD(Material.NAME_TAG, "전적", "개인 전적을 확인합니다.", player -> player.performCommand("전적")),
        ACHIEVEMENT(Material.BOOK, "업적", "업적 목록을 확인합니다.", player -> player.performCommand("업적")),
        OPTION(Material.REDSTONE_COMPARATOR, "설정", "설정 관련 메뉴를 확인합니다.", player -> {
            PlayerOption playerOption = PlayerOption.getInstance();
            playerOption.open(player);
        }),
        CORE(Material.FIREWORK_CHARGE, "코어 확인", "전투원에 할당된 코어를 확인합니다.", player -> player.performCommand("코어")),
        COMMAND(Material.COMMAND, "명령어 목록", "서버 명령어 목록을 확인합니다.", player -> {
            player.performCommand("명령어");
            player.closeInventory();
        }),
        CHAT_BLOCK(Material.BARRIER, "차단 목록", "차단된 플레이어 목록을 확인합니다.", player -> player.performCommand("차단 목록")),
        EXIT(new ButtonItem.EXIT("MenuExit"));

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        MenuItem(Material material, String name, String lore, Consumer<Player> action) {
            guiItem = new GuiItem("Menu" + this, new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build()) {
                @Override
                public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                    if (clickType != ClickType.LEFT)
                        return false;

                    action.accept(player);
                    return true;
                }
            };
        }
    }
}
