package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.game.mode.GamePlayMode;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.MessageFormat;
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
        super(2, "§8게임 시작", player);

        fillAll(GUIItem.EMPTY);
        set(0, 0, SelectGameInfoItem.NORMAL.definedItem);
        set(1, 0, SelectGameInfoItem.RANK.definedItem, itemBuilder ->
                itemBuilder.formatLore(GeneralConfig.getGameConfig().getRankPlacementPlayCount()));
        set(1, 8, new GUIItem.Previous(Warp::new));

        for (int i = 0; i < GeneralConfig.getGameConfig().getMaxRoomCount(); i++) {
            set(0, i + 2, SelectGameItem.NORMAL.create(i));
            set(1, i + 2, SelectGameItem.RANK.create(i));
        }
    }

    /**
     * 게임 정보 아이템.
     */
    private enum SelectGameInfoItem {
        NORMAL(false, Material.IRON_SWORD, "§a§l일반",
                "§f랭크 점수에 반영되지 않는 일반 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :"),
        RANK(true, Material.DIAMOND_SWORD, "§6§l랭크",
                "§f랭크 점수에 반영되는 랭크 게임입니다.",
                "§f랭크 게임 §e{0}판§f을 플레이하면 첫 티어 및 랭크 점수가 결정됩니다.",
                "§f중간 난입이 불가능한 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :");

        /** GUI 아이템 */
        private final DefinedItem definedItem;

        SelectGameInfoItem(boolean isRanked, Material material, String name, String... lores) {
            ItemBuilder itemBuilder = new ItemBuilder(material).setName(name).setLore(lores);

            String[] gamePlayModeNames = Arrays.stream(GamePlayMode.values())
                    .filter(gamePlayMode -> gamePlayMode.isRanked() == isRanked)
                    .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);
            itemBuilder.addLore(gamePlayModeNames);

            this.definedItem = new DefinedItem(itemBuilder.build());
        }
    }

    /**
     * 게임 방 아이템.
     */
    @AllArgsConstructor
    private enum SelectGameItem {
        NORMAL("§a일반 게임 {0}", false,
                GeneralConfig.getGameConfig().getNormalMinPlayerCount(), GeneralConfig.getGameConfig().getNormalMaxPlayerCount()),
        RANK("§6랭크 게임 {0}", true,
                GeneralConfig.getGameConfig().getRankMinPlayerCount(), GeneralConfig.getGameConfig().getRankMaxPlayerCount());

        private final String name;
        private final boolean isRanked;
        private final int minPlayerCount;
        private final int maxPlayerCount;

        @NonNull
        private static String getPhaseName(@NonNull GameRoom gameRoom) {
            switch (gameRoom.getPhase()) {
                case WAITING:
                    return "§c대기 중";
                case PLAYING:
                    if (gameRoom.getGame().isPlaying())
                        return "§a게임 진행";
                    else
                        return "§a게임 준비";
                case FINISHED:
                default:
                    return "§c종료됨";
            }
        }

        /**
         * 지정한 방 번호에 해당하는 게임 방 아이템을 생성하여 반환한다.
         *
         * @param number 방 번호
         * @return 게임 방 아이템
         */
        @NonNull
        private DefinedItem create(int number) {
            ItemBuilder itemBuilder = new ItemBuilder(Material.STAINED_GLASS_PANE)
                    .setDamage((short) 5)
                    .setName(MessageFormat.format(name, number))
                    .setLore("§e인원 수 : §f[{0}§f/{1} 명]",
                            "§e상태 : §f{2}",
                            "§e게임 모드 : §f{3}",
                            "§e경과 시간 : §f{4}");

            GameRoom gameRoom = GameRoom.fromNumber(isRanked, number);

            String gameUserCount = "0";
            String phase = "§8--";
            String playMode = "§8--";
            String displayTime = "§8--";
            if (gameRoom != null) {
                gameUserCount = (gameRoom.getUsers().size() >= minPlayerCount ? "§f" : "§c") + gameRoom.getUsers().size();
                phase = getPhaseName(gameRoom);

                if (gameRoom.getPhase() == GameRoom.Phase.PLAYING) {
                    playMode = gameRoom.getGame().getGamePlayMode().getName();
                    displayTime = DurationFormatUtils.formatDuration(gameRoom.getGame().getElapsedTime().toMilliseconds(), "mm:ss", true);
                }
            }

            itemBuilder.formatLore(gameUserCount, maxPlayerCount, phase, playMode, displayTime);

            return new DefinedItem(itemBuilder.build(), (clickType, player) -> {
                if (clickType != ClickType.LEFT)
                    return false;

                User user = User.fromPlayer(player);
                if (user.getGameRoom() == null) {
                    GameRoom selectGameRoom = GameRoom.fromNumber(isRanked, number);
                    if (selectGameRoom == null)
                        selectGameRoom = new GameRoom(isRanked, number);

                    user.joinGame(selectGameRoom);

                    player.closeInventory();
                }

                return true;
            });
        }
    }
}
