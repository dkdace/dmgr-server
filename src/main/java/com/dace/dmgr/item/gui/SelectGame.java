package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GamePlayMode;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * 게임 입장 GUI 클래스.
 */
public final class SelectGame extends Gui {
    @Getter
    private static final SelectGame instance = new SelectGame();
    /** 이전 버튼 GUI 아이템 객체 */
    private static final GuiItem buttonLeft = new ButtonItem.Left("SelectGameLeft") {
        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            Warp.getInstance().open(player);
            return true;
        }
    };

    private SelectGame() {
        super(2, "§8게임 시작");
    }

    /**
     * GUI에 게임 방 정보를 표시한다.
     *
     * @param guiController GUI 컨트롤러 객체
     * @param game          게임
     * @param isRanked      랭크 여부
     * @param i             방 번호
     */
    private void displayGameInfo(@NonNull GuiController guiController, @Nullable Game game, boolean isRanked, int i) {
        int minPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMinPlayerCount() : GeneralConfig.getGameConfig().getNormalMinPlayerCount();
        int maxPlayerCount = isRanked ? GeneralConfig.getGameConfig().getRankMaxPlayerCount() : GeneralConfig.getGameConfig().getNormalMaxPlayerCount();
        int index = isRanked ? i + 11 : i + 2;
        GuiItem guiItem = isRanked ? SelectGameItem.RANK.guiItem : SelectGameItem.NORMAL.guiItem;

        if (game == null)
            guiController.set(index, guiItem, itemBuilder -> itemBuilder.formatName(i)
                    .formatLore(0, maxPlayerCount, "§8--", "§8--", "§8--"));
        else {
            long playTime = System.currentTimeMillis() - game.getStartTime();

            String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);
            String gameUserCountColor = game.getGameUsers().size() >= minPlayerCount ? "§f" : "§c";

            guiController.set(index, guiItem, itemBuilder -> itemBuilder
                    .formatName(i)
                    .formatLore(gameUserCountColor + game.getGameUsers().size(),
                            maxPlayerCount,
                            (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                            (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                            (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime)));
        }
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getStaticItem());

        guiController.set(0, SelectGameInfoItem.NORMAL.staticItem);
        guiController.set(9, SelectGameInfoItem.RANK.staticItem, itemBuilder ->
                itemBuilder.formatLore(GeneralConfig.getGameConfig().getRankPlacementPlayCount()));
        guiController.set(17, buttonLeft);

        for (int i = 0; i < GeneralConfig.getGameConfig().getMaxRoomCount(); i++) {
            Game normalGame = Game.fromNumber(false, i);
            Game rankGame = Game.fromNumber(true, i);

            displayGameInfo(guiController, normalGame, false, i);
            displayGameInfo(guiController, rankGame, true, i);
        }
    }

    private enum SelectGameInfoItem {
        NORMAL(Material.IRON_SWORD, "§a§l일반", "§f랭크 점수에 반영되지 않는 일반 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :"),
        RANK(Material.DIAMOND_SWORD, "§6§l랭크", "§f랭크 점수에 반영되는 랭크 게임입니다.",
                "§f랭크 게임 §e{0}판§f을 플레이하면 첫 티어 및 랭크 점수가 결정됩니다.",
                "§f중간 난입이 불가능한 게임입니다.",
                "",
                "§f다음 게임 모드 중에서 무작위로 선택됨 :");

        /** 정적 아이템 객체 */
        private final StaticItem staticItem;

        SelectGameInfoItem(Material material, String name, String... lores) {
            ItemBuilder itemBuilder = new ItemBuilder(material).setName(name);
            for (String lore : lores)
                itemBuilder.addLore("§f" + lore);

            String[] gamePlayModeNames = Arrays.stream(GamePlayMode.values())
                    .filter(gamePlayMode -> this.toString().equals("NORMAL"))
                    .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);
            itemBuilder.addLore(gamePlayModeNames);

            this.staticItem = new StaticItem("SelectGameInfoItem" + this, itemBuilder.build());
        }
    }

    private enum SelectGameItem {
        NORMAL(Material.STAINED_GLASS_PANE, ((short) 5), "§a일반 게임 {0}"),
        RANK(Material.STAINED_GLASS_PANE, ((short) 5), "§6랭크 게임 {0}");

        /** GUI 아이템 객체 */
        private final GuiItem guiItem;

        SelectGameItem(Material material, int damage, String name) {
            this.guiItem = new GuiItem("SelectGameItem" + this, new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(name)
                    .setLore("§e인원 수 : §f[{0}§f/{1} 명]",
                            "§e상태 : §f{2}",
                            "§e게임 모드 : §f{3}",
                            "§e경과 시간 : §f{4}")
                    .build()) {
                @Override
                public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
                    if (clickType != ClickType.LEFT)
                        return false;

                    String displayName = clickItem.getItemMeta().getDisplayName();
                    int number = Integer.parseInt(String.valueOf(displayName.charAt(displayName.length() - 1)));
                    boolean isRanked = SelectGameItem.this == SelectGameItem.RANK;

                    User user = User.fromPlayer(player);
                    GameUser gameUser = GameUser.fromUser(user);
                    Game game = Game.fromNumber(isRanked, number);
                    if (gameUser == null) {
                        if (game == null)
                            game = new Game(isRanked, number);
                        if (!game.canJoin())
                            return false;

                        new GameUser(user, game);

                        player.closeInventory();
                    }

                    return true;
                }
            };
        }
    }
}
