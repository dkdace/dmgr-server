package com.dace.dmgr.item.gui;

import com.dace.dmgr.command.MenuCommand;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Consumer;

/**
 * 메뉴 GUI 클래스.
 *
 * @see MenuCommand
 */
public final class Menu extends ChestGUI {
    /**
     * 메뉴 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public Menu(@NonNull Player player) {
        super(6, "§8메뉴", player);

        fillAll(GUIItem.EMPTY);

        new AsyncTask<>((onFinish, onError) ->
                set(0, 4, new DefinedItem(UserData.fromPlayer(player).getProfileItem())));

        set(2, 1, MenuItem.GAME_START.definedItem);
        set(2, 3, MenuItem.RECORD.definedItem);
        set(2, 5, MenuItem.CORE.definedItem);
        set(2, 7, MenuItem.ACHIEVEMENT.definedItem);
        set(4, 1, MenuItem.OPTION.definedItem);
        set(4, 3, MenuItem.COMMAND.definedItem);
        set(4, 5, MenuItem.RANKING.definedItem);
        set(4, 7, MenuItem.LOBBY.definedItem);
        set(5, 8, GUIItem.EXIT);
    }

    /**
     * 메뉴의 아이템 목록.
     */
    private enum MenuItem {
        GAME_START(Material.IRON_SWORD, 0, "게임 시작", "게임에 참가합니다.", Warp::new),
        RECORD(Material.NAME_TAG, 0, "전적", "개인 전적을 확인합니다.", player -> new Stat(player, UserData.fromPlayer(player))),
        ACHIEVEMENT(Material.BOOK, 0, "업적", "업적 목록을 확인합니다.", player -> player.performCommand("업적")),
        OPTION(Material.REDSTONE_COMPARATOR, 0, "설정", "설정 관련 메뉴를 확인합니다.", PlayerOption::new),
        CORE(Material.FIREWORK_CHARGE, 0, "코어 확인", "전투원에 할당된 코어를 확인합니다.", CoreList::new),
        COMMAND(Material.COMMAND, 0, "명령어 목록", "서버 명령어 목록을 확인합니다.",
                player -> {
                    player.performCommand("명령어");
                    player.closeInventory();
                }),
        RANKING(Material.DIAMOND, 0, "랭킹", "1위부터 10위까지의 항목별 랭킹을 확인합니다.", Ranking::new),
        LOBBY(Material.BED, 14, "로비", "로비로 이동합니다.", player -> player.performCommand("exit"));

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        MenuItem(Material material, int damage, String name, String lore, Consumer<Player> action) {
            definedItem = new DefinedItem(new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName("§e§l" + name)
                    .setLore("§f" + lore)
                    .build(),
                    (clickType, player) -> {
                        if (clickType != ClickType.LEFT)
                            return false;

                        action.accept(player);
                        return true;
                    });
        }
    }
}
