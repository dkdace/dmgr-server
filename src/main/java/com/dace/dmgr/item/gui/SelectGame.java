package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.PlayerSkin;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;

/**
 * 게임 입장 GUI 클래스.
 */
public final class SelectGame extends ChestGUI {
    /**
     * 게임 입장 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public SelectGame(@NonNull Player player) {
        super(1, "§8게임 시작", player);

        fillAll(GUIItem.EMPTY);
        set(0, 3, SelectGameInfoItem.NORMAL.definedItem);
        set(0, 5, SelectGameInfoItem.RANK.definedItem, itemBuilder ->
                itemBuilder.formatLore(GeneralConfig.getGameConfig().getRankPlacementPlayCount()));
    }

    /**
     * 게임 정보 아이템.
     */
    private enum SelectGameInfoItem {
        NORMAL(false, "YzQ2NWMxMjE5NThjMDUyMmUzZGNjYjNkMTRkNjg2MTJkNjMxN2NkMzgwYjBlNjQ2YjYxYjc0MjBiOTA0YWYwMiJ9fX0=", "§a§l일반",
                "§f랭크 점수에 반영되지 않는 일반 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :"),
        RANK(true, "YzM0NTJiOThhYjlhODhkMTc1N2YwMzJjMDcyYWY4MWNmYTM1ZGRiNDc5NDU4NTkxNDc4MTFiY2RjZmQ5ODcxZSJ9fX0=", "§6§l랭크",
                "§f랭크 점수에 반영되는 랭크 게임입니다.",
                "§f랭크 게임 §e{0}판§f을 플레이하면 첫 티어 및 랭크 점수가 결정됩니다.",
                "§f중간 난입이 불가능한 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :");

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        SelectGameInfoItem(boolean isRanked, String skinUrl, String name, String... lores) {
            ItemBuilder itemBuilder = new ItemBuilder(PlayerSkin.fromURL(skinUrl)).setName(name).setLore(lores);

            String[] gamePlayModeNames = Arrays.stream(GamePlayMode.values())
                    .filter(gamePlayMode -> gamePlayMode.isRanked() == isRanked)
                    .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);
            itemBuilder.addLore(gamePlayModeNames);
            itemBuilder.addLore("", "§7§n좌클릭§f하여 참여 가능한 게임에 입장합니다.", "§7§n우클릭§f하여 게임 방을 선택합니다.");

            if (isRanked)
                this.definedItem = new DefinedItem(itemBuilder.build(), new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                    User.fromPlayer(player).sendMessageWarn("랭크 게임 준비 중입니다.");
                    player.closeInventory();

                    return false;
                }));
            else
                this.definedItem = new DefinedItem(itemBuilder.build(),
                        new DefinedItem.ClickHandler(ClickType.LEFT, player -> {
                            User user = User.fromPlayer(player);

                            if (user.getGameRoom() != null)
                                return false;

                            GameRoom gameRoom = GameRoom.getAvailableGameRoom(false);
                            if (gameRoom == null)
                                return false;

                            user.joinGame(gameRoom);

                            player.closeInventory();

                            return true;
                        }),
                        new DefinedItem.ClickHandler(ClickType.RIGHT, player -> {
                            new SelectGameRoom(player, false);
                            return true;
                        }));
        }
    }
}
