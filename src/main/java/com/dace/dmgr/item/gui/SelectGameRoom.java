package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.game.GameRoom;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.PlayerSkullUtil;
import com.dace.dmgr.user.User;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.text.MessageFormat;

/**
 * 게임 방 선택 GUI 클래스.
 */
public final class SelectGameRoom extends ChestGUI {
    /**
     * 게임 방 선택 GUI 인스턴스를 생성한다.
     *
     * @param player   GUI 표시 대상 플레이어
     * @param isRanked 랭크 여부
     */
    public SelectGameRoom(@NonNull Player player, boolean isRanked) {
        super(1, "§8게임 시작", player);

        fillAll(GUIItem.EMPTY);

        for (int i = 0; i < GeneralConfig.getGameConfig().getMaxRoomCount(); i++)
            set(0, i, (isRanked ? SelectGameItem.RANK : SelectGameItem.NORMAL).create(i));

        set(0, 8, new GUIItem.Previous(SelectGame::new));
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

        /** 게임 방 아이템의 스킨 URL 목록 */
        private static final String[] SKIN_URLS = {
                "OTBmYmRmMjYwZTJjZjIwMjViZmJjMzA0ZmE3YjgxNTkzMDE2ZDA3MjliMDBkYjE2ZjA5ZWQwMWY5YmQzZTY5OSJ9fX0=",
                "MjFkODU5ZThiMTRmNjI2NDY4NTljZjM4MDRhNjRmMTA2MGQ2ODc5MzQxYjRjMzM4NWI0NmEwZWM0MGZhZjczYyJ9fX0=",
                "YjNkOTNlOGI1ZmIwYjVkNTBhYmQ0ZWY4ODUzMmY0Njg3NGI5OTI0ZjY2OGRkYjAxMDkxNDY4ZTRlNjFiOWM4MyJ9fX0=",
                "ZWE3OWRhYTkyMDhhMDU2NTAwYzgzY2QyMDkwZmFlYzkxZWFlNTQwNTc5MmU0ZjU0NzI2OTdiMGU3ZGFjMzIzYSJ9fX0=",
                "MjFiZDg5OGNjYjFlNWFmNzFkNjQ0MTFhMTU5YzBkNDY5YWY2MzkxMDYyZTgwMmJmZTgxOTk1NjVlOGQ3YmU2OCJ9fX0=",
                "MzNjZDkzNGYxMWYwNzY2ZjU0MTBlYmE5ZTdiNWYwY2ViNjZmNmIzMTdlODQ1Y2I2YTUwMWYzNzI1ODU1NmE0MyJ9fX0=",
                "Nzk1NTQ0YTQwODBmNmE0YWYxOWUwODAxZGE0OTI4MzhmYjA0YmM1MzRmYzg0NjAwZTA0Yzg4MTVlMTMxZTI5ZCJ9fX0=",
                "ZjQzZjhiMTA1ZWMzYjQ4OTYzZTk4MWFmZjgyNWM1YWI5NGRmZmVlNWQ4NGE4NjlhZDA3MmFmYThmZDIxNGU3ZSJ9fX0=",
                "Y2VmODNkMDI3OWEyZTUxZTdlNTMyZDIwMmRmNWVjN2RiODNmOTZkM2ZiYjI0NWRhMWI2MjhjMWYwYjFlZWNiZCJ9fX0=",
                "N2U0ZWFiNWM4YzgyZTBhOTE4MjhmMGE1ZGJkNDNkMjYwNzZiYzRiNTdjZTFkMTM1ZWNhMmQ3YmQwYjFkZTMwIn19fQ=="
        };
        /** 플레이 중인 게임 방 아이템의 스킨 URL 목록 */
        private static final String[] SKIN_URLS_PLAYING = {
                "NGMyNDJhNTkzOTI4YTgyYTI0ZWZkZDdhZjA5NGNiZDA2MjVmMzA5MWE1YmU4ZGY1MmIzNGYyYjU4ZWQyNWIifX19",
                "NzMwNDFjMmVhODI3YmY2NjFiNjQ3MmE4ZTMxMGY2NTFiMjk0ODNhNWZkN2U3NDEzOTdmOTZjMTUxNWIxNjgifX19",
                "ZTE3MzgxYWE0Y2M3M2Q5ZDc5ODk0MTc0NTc2ZWJmMzRmN2ZlNDAzNDZhYmI4YzcxYWRjNTczZWE4YzdlYzUifX19",
                "YmJlNTk3M2U0ZTc5MTYyMzk5M2U2N2MyNDliNzJmM2M1MTYwZjkxZDY5MjZhM2E4NzU2YmM1NDNmMzgzNTIzMiJ9fX0=",
                "MTgzYzZiYmRiYTZhODc3Zjk5NmQ4YzNkZDFkOWI4NDUwODViZTFiZGIyZjUzNWUyYjZmOGZjM2M2NTg5YjEifX19",
                "ODJlODViOTMxYjY4ZDZjZDk1YmFjZGExOGRkOGMwODZjZmJlNmIzZjc5ZTY0ZTNiZDNjOGVlMGRhNmE5ZDMyIn19fQ==",
                "YzM0NWQxNjM3ODg0NjhiNTQ3OTUxNzg3ZWUyOTQzMzFjZmRkOWJkMTUxNGMyNDI0YzRkNDU5YzVhMDY5NjljMCJ9fX0=",
                "NDM5MDk0OTY3Y2E2NmQ4OTJjNmVmMTU4Y2U4MjllOTA0YzllMjRjODY0ZWRmMjc3MTFkNDEwZWY2YzQyMmI3ZCJ9fX0=",
                "YmNkZmYzNjVhZTdlMzhhZTI3YjFkNGNmZWUxNTg0OGNhYmY3NDZhMTc4NWVlZDE4MTUzNmE3ZmY1YzM4ZSJ9fX0=",
                "YjQxNmVlZDc5MjU5NTA1NzliMzkxZWQxZTEzMDQyNmQ1NjE5NzZkZDYxNzE1ZDBhMmQzNWNlOTA0Y2Y0In19fQ=="
        };

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
            GameRoom gameRoom = GameRoom.fromNumber(isRanked, number);

            ItemBuilder itemBuilder = new ItemBuilder(PlayerSkullUtil.fromURL(
                    (gameRoom != null && gameRoom.getPhase() == GameRoom.Phase.PLAYING ? SKIN_URLS_PLAYING : SKIN_URLS)[number]))
                    .setName(MessageFormat.format(name, number))
                    .setLore("§e인원 수 : §f[{0}§f/{1} 명]",
                            "§e상태 : §f{2}",
                            "§e게임 모드 : §f{3}",
                            "§e경과 시간 : §f{4}");

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
