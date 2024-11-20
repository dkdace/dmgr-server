package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * 메뉴 GUI 클래스.
 */
public final class Menu extends Gui {
    /** 플레이어 GUI 아이템 객체 */
    private static final StaticItem playerInto = new StaticItem("MenuPlayer", new ItemBuilder(Material.SKULL_ITEM)
            .setDamage((short) 3)
            .build());
    @Getter
    private static final Menu instance = new Menu();

    private Menu() {
        super(6, "§8메뉴");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getStaticItem());

        new AsyncTask<Void>((onFinish, onError) ->
                guiController.set(4, playerInto, itemBuilder -> {
                    UserData userData = UserData.fromPlayer(player);
                    itemBuilder.setName(userData.getDisplayName()).setSkullOwner(player);
                })
        );

        guiController.set(19, MenuItem.GAME_START.guiItem);
        guiController.set(21, MenuItem.RECORD.guiItem);
        guiController.set(23, MenuItem.CORE.guiItem);
        guiController.set(25, MenuItem.ACHIEVEMENT.guiItem);
        guiController.set(37, MenuItem.OPTION.guiItem);
        guiController.set(39, MenuItem.COMMAND.guiItem);
        guiController.set(41, MenuItem.RANKING.guiItem);
        guiController.set(43, MenuItem.LOBBY.guiItem);
        guiController.set(53, ButtonItem.Exit.getInstance());
    }

    @AllArgsConstructor
    private enum MenuItem {
        GAME_START(Material.IRON_SWORD, 0, "게임 시작", "게임에 참가합니다.",
                player -> Warp.getInstance().open(player)),
        RECORD(Material.NAME_TAG, 0, "전적", "개인 전적을 확인합니다.",
                player -> player.performCommand("전적")),
        ACHIEVEMENT(Material.BOOK, 0, "업적", "업적 목록을 확인합니다.",
                player -> player.performCommand("업적")),
        OPTION(Material.REDSTONE_COMPARATOR, 0, "설정", "설정 관련 메뉴를 확인합니다.",
                player -> PlayerOption.getInstance().open(player)),
        CORE(Material.FIREWORK_CHARGE, 0, "코어 확인", "전투원에 할당된 코어를 확인합니다.",
                player -> new CoreList(User.fromPlayer(player).getUserData()).open(player)),
        COMMAND(Material.COMMAND, 0, "명령어 목록", "서버 명령어 목록을 확인합니다.",
                player -> {
                    player.performCommand("명령어");
                    player.closeInventory();
                }),
        RANKING(Material.DIAMOND, 0, "랭킹", "1위부터 10위까지의 항목별 랭킹을 확인합니다.",
                player -> Ranking.getInstance().open(player)),
        LOBBY(Material.BED, 14, "로비", "로비로 이동합니다.",
                player -> player.performCommand("exit"));

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        MenuItem(Material material, int damage, String name, String lore, Consumer<Player> action) {
            guiItem = new GuiItem("Menu" + this, new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .setDamage((short) damage)
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
