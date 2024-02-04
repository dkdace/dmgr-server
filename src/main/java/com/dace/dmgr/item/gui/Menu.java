package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * 메뉴 GUI 클래스.
 */
public final class Menu extends Gui {
    @Getter
    private static final Menu instance = new Menu();

    public Menu() {
        super(6, "§8메뉴");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getGuiItem());
        guiController.set(4, PlayerInfoItem.instance, itemBuilder -> {
            String prefix = UserData.fromPlayer(player).getLevelPrefix();
            ((SkullMeta) itemBuilder.formatName(prefix, player.getName()).getItemMeta()).setOwningPlayer(player);
        });
        guiController.set(19, MenuItem.GAME_START.guiItem);
        guiController.set(21, MenuItem.WARP.guiItem);
        guiController.set(23, MenuItem.RECORD.guiItem);
        guiController.set(25, MenuItem.ACHIEVEMENT.guiItem);
        guiController.set(37, MenuItem.OPTION.guiItem);
        guiController.set(39, MenuItem.CORE.guiItem);
        guiController.set(41, MenuItem.COMMAND.guiItem);
        guiController.set(43, MenuItem.CHAT_BLOCK.guiItem);
        guiController.set(53, ButtonItem.EXIT.getGuiItem());
    }

    @Override
    public void onClick(InventoryClickEvent event, @NonNull Player player, @NonNull GuiItem<?> guiItem) {
        if (event.getClick() != ClickType.LEFT)
            return;

        if (guiItem.getGui() == null) {
            if (guiItem == ButtonItem.EXIT.getGuiItem())
                player.closeInventory();

            return;
        }

        switch ((MenuItem) guiItem.getIdentifier()) {
            case GAME_START:
                SelectGame.getInstance().open(player);
                break;
            case WARP:
                player.performCommand("이동");
                break;
            case RECORD:
                player.performCommand("전적");
                break;
            case ACHIEVEMENT:
                player.performCommand("업적");
                break;
            case OPTION:
                player.performCommand("설정");
                break;
            case COMMAND:
                player.performCommand("명령어");
                break;
            case CHAT_BLOCK:
                player.performCommand("차단 목록");
                break;
        }
    }

    @AllArgsConstructor
    private enum MenuItem {
        PLAYER(PlayerInfoItem.instance),
        GAME_START(Material.IRON_SWORD, "게임 시작", "전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다."),
        WARP(Material.LEATHER_BOOTS, "이동", "원하는 장소로 이동합니다."),
        RECORD(Material.NAME_TAG, "전적", "개인 전적을 확인합니다."),
        ACHIEVEMENT(Material.BOOK, "업적", "업적 목록을 확인합니다."),
        OPTION(Material.REDSTONE_COMPARATOR, "설정", "설정 관련 메뉴를 확인합니다."),
        CORE(Material.FIREWORK_CHARGE, "코어 확인", "전투원에 할당된 코어를 확인합니다."),
        COMMAND(Material.COMMAND, "명령어 목록", "서버 명령어 목록을 확인합니다."),
        CHAT_BLOCK(Material.BARRIER, "차단 목록", "차단된 플레이어 목록을 확인합니다.");

        /** GUI 아이템 객체 */
        private final GuiItem<MenuItem> guiItem;

        MenuItem(Material material, String name, String lore) {
            guiItem = new GuiItem<MenuItem>(this, new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return true;
                }
            };
        }
    }

    private static final class PlayerInfoItem extends GuiItem<MenuItem> {
        private static final PlayerInfoItem instance = new PlayerInfoItem();

        private PlayerInfoItem() {
            super(MenuItem.PLAYER, new ItemBuilder(Material.SKULL_ITEM)
                    .setDamage((short) 3)
                    .setName("{0} §f{1}")
                    .build());
        }

        @Override
        public Gui getGui() {
            return Menu.instance;
        }

        @Override
        public boolean isClickable() {
            return false;
        }
    }
}
