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

    public Warp() {
        super(1, "§8이동");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.set(0, WarpItem.GAME_START.guiItem);
        guiController.set(1, WarpItem.WARP.guiItem);
        guiController.set(2, WarpItem.RECORD.guiItem);
        guiController.set(7, WarpItem.LEFT.guiItem);
        guiController.set(8, WarpItem.EXIT.guiItem);
    }

    @AllArgsConstructor
    private enum WarpItem {
        GAME_START(Material.GOLD_SWORD, 0, "자유 전투", "전장에서 다른 플레이어들과 자유롭게 전투합니다.",
                player -> FreeCombat.start(User.fromPlayer(player))),
        WARP(Material.ARMOR_STAND, 0, "훈련장", "훈련장에서 다양한 전투원을 체험하고 전투 기술을 훈련합니다.", player -> {
        }),
        RECORD(Material.BED, 14, "로비", "로비로 이동합니다.", player -> player.performCommand("exit")),
        LEFT(new ButtonItem.LEFT("WarpLeft") {
            @Override
            public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                player.performCommand("메뉴");
                return true;
            }
        }),
        EXIT(new ButtonItem.EXIT("WarpExit"));

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        WarpItem(Material material, int damage, String name, String lore, Consumer<Player> action) {
            guiItem = new GuiItem("Warp" + this, new ItemBuilder(material)
                    .setName("§e§l" + name)
                    .setDamage((short) damage)
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
