package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.Consumer;

/**
 * 이동 GUI 클래스.
 */
public final class Warp extends ChestGUI {
    /**
     * 이동 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public Warp(@NonNull Player player) {
        super(1, "§8이동", player);

        fillAll(GUIItem.EMPTY);
        set(0, 1, WarpItem.TEAM_GAME.definedItem);
        set(0, 3, WarpItem.FREE_GAME.definedItem);
        set(0, 5, WarpItem.TRAINING.definedItem);
        set(0, 7, new GUIItem.Previous(Menu::new));
        set(0, 8, GUIItem.EXIT);
    }

    /**
     * 이동 아이템 목록.
     */
    private enum WarpItem {
        TEAM_GAME(Material.IRON_SWORD, "팀전 (일반/랭크)", "전장에서 다른 플레이어들과 팀을 맺어 전투하고 보상을 획득합니다.", SelectGame::new),
        FREE_GAME(Material.GOLD_SWORD, "자유 전투", "전장에서 다른 플레이어들과 자유롭게 전투합니다.",
                player -> FreeCombat.start(User.fromPlayer(player))),
        TRAINING(Material.ARMOR_STAND, "훈련장", "훈련장에서 다양한 전투원을 체험하고 전투 기술을 훈련합니다.",
                player -> {
                });

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        WarpItem(Material material, String name, String lore, Consumer<Player> action) {
            definedItem = new DefinedItem(new ItemBuilder(material)
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
