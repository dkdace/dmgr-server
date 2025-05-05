package com.dace.dmgr.menu;

import com.dace.dmgr.command.RankingCommand;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUIItem;
import com.dace.dmgr.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * 랭킹 GUI 클래스.
 *
 * @see RankingCommand
 */
public final class Ranking extends ChestGUI {
    /**
     * 랭킹 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    Ranking(@NonNull Player player) {
        super(1, "§8랭킹", player);

        set(0, 0, RankingItem.RANK_RATE.definedItem);
        set(0, 1, RankingItem.LEVEL.definedItem);
        set(0, 7, new GUIItem.Previous(Menu::new));
        set(0, 8, GUIItem.EXIT);
    }

    /**
     * 랭킹의 아이템 목록.
     */
    private enum RankingItem {
        RANK_RATE(Material.DIAMOND, "랭크 점수 (티어)", "점수"),
        LEVEL(Material.EXP_BOTTLE, "레벨", "레벨");

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        RankingItem(Material material, String name, String rankType) {
            this.definedItem = new DefinedItem(new ItemBuilder(material).setName("§e§l" + name).build(),
                    new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                        player.performCommand("랭킹 " + rankType);
                        player.closeInventory();

                        return true;
                    }));
        }
    }
}
