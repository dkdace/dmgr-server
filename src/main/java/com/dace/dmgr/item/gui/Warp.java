package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * 이동 GUI 클래스.
 */
public final class Warp extends Gui {
    @Getter
    private static final Warp instance = new Warp();

    private Warp() {
        super(1, "§8이동");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getStaticItem());

        guiController.set(1, WarpItem.TEAM_GAME.guiItem);
        guiController.set(3, WarpItem.FREE_GAME.guiItem);
        guiController.set(5, WarpItem.TRAINING.guiItem);
        guiController.set(7, WarpItem.LEFT.guiItem);
        guiController.set(8, ButtonItem.Exit.getInstance());
    }

    @AllArgsConstructor
    private enum WarpItem {
        TEAM_GAME(Material.IRON_SWORD, "팀전 (일반/랭크)", "전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.",
                player -> SelectGame.getInstance().open(player)),
        FREE_GAME(Material.GOLD_SWORD, "자유 전투", "전장에서 다른 플레이어들과 자유롭게 전투합니다.",
                player -> FreeCombat.start(User.fromPlayer(player))),
        TRAINING(Material.ARMOR_STAND, "훈련장", "훈련장에서 다양한 전투원을 체험하고 전투 기술을 훈련합니다.",
                player -> {
                }),
        LEFT(new ButtonItem.Left("WarpLeft") {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                player.performCommand("메뉴");
                return true;
            }
        });

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        WarpItem(Material material, String name, String lore, Consumer<Player> action) {
            guiItem = new GuiItem("Warp" + this, new ItemBuilder(material)
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
