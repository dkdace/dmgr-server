package com.dace.dmgr.item.gui;

import com.dace.dmgr.item.ItemBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * 랭킹 GUI 클래스.
 */
public final class Ranking extends Gui {
    @Getter
    private static final Ranking instance = new Ranking();

    private Ranking() {
        super(1, "§8랭킹");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.set(0, RankingItem.RANK_RATE.guiItem);
        guiController.set(1, RankingItem.LEVEL.guiItem);
        guiController.set(7, RankingItem.LEFT.guiItem);
        guiController.set(8, RankingItem.EXIT.guiItem);
    }

    @AllArgsConstructor
    private enum RankingItem {
        RANK_RATE(Material.DIAMOND, "랭크 점수 (티어)",
                player -> player.performCommand("랭킹 점수")),
        LEVEL(Material.EXP_BOTTLE, "레벨",
                player -> player.performCommand("랭킹 레벨")),
        LEFT(new ButtonItem.LEFT("RankingLeft") {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                player.performCommand("메뉴");
                return true;
            }
        }),
        EXIT(new ButtonItem.EXIT("RankingExit"));

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        RankingItem(Material material, String name, Consumer<Player> action) {
            guiItem = new GuiItem("Ranking" + this, new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .build()) {
                @Override
                public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                    if (clickType != ClickType.LEFT)
                        return false;

                    action.accept(player);
                    player.closeInventory();
                    return true;
                }
            };
        }
    }
}
