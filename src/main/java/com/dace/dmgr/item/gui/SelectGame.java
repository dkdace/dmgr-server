package com.dace.dmgr.item.gui;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GamePlayMode;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

/**
 * 게임 입장 GUI 클래스.
 */
public final class SelectGame extends Gui {
    @Getter
    private static final SelectGame instance = new SelectGame();

    public SelectGame() {
        super(2, "§8게임 시작");
    }

    /**
     * GUI에 일반 게임 방 목록을 표시한다.
     *
     * @param guiController GUI 컨트롤러 객체
     */
    private void displayNormalRooms(@NonNull GuiController guiController) {
        for (int i = 0; i < GeneralConfig.getGameConfig().getMaxRoomCount(); i++) {
            final int index = i;

            Game game = Game.fromNumber(false, i);

            if (game == null)
                guiController.set(2 + i, SelectGameItem.NORMAL.guiItem, itemBuilder -> itemBuilder.formatName(index)
                        .formatLore(0,
                                GeneralConfig.getGameConfig().getNormalMaxPlayerCount(),
                                "§8--",
                                "§8--",
                                "§8--"));
            else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);
                String gameUserCountColor = game.getGameUsers().size() >= GeneralConfig.getGameConfig().getNormalMinPlayerCount() ? "§f" : "§c";

                guiController.set(2 + i, SelectGameItem.NORMAL.guiItem, itemBuilder -> itemBuilder
                        .formatName(index)
                        .formatLore(gameUserCountColor + game.getGameUsers().size(),
                                GeneralConfig.getGameConfig().getNormalMaxPlayerCount(),
                                (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime)));
            }
        }
    }

    /**
     * GUI에 랭크 게임 방 목록을 표시한다.
     *
     * @param guiController GUI 컨트롤러 객체
     */
    private void displayRankRooms(@NonNull GuiController guiController) {
        for (int i = 0; i < GeneralConfig.getGameConfig().getMaxRoomCount(); i++) {
            final int index = i;

            Game game = Game.fromNumber(true, i);

            if (game == null)
                guiController.set(11 + i, SelectGameItem.RANK.guiItem, itemBuilder -> itemBuilder.formatName(index)
                        .formatLore(0,
                                GeneralConfig.getGameConfig().getRankMaxPlayerCount(),
                                "§8--",
                                "§8--",
                                "§8--"));
            else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);
                String gameUserCountColor = game.getGameUsers().size() >= GeneralConfig.getGameConfig().getRankMinPlayerCount() ? "§f" : "§c";

                guiController.set(11 + i, SelectGameItem.RANK.guiItem, itemBuilder -> itemBuilder
                        .formatName(index)
                        .formatLore(gameUserCountColor + game.getGameUsers().size(),
                                GeneralConfig.getGameConfig().getRankMaxPlayerCount(),
                                (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime)
                        ));
            }
        }
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillAll(DisplayItem.EMPTY.getGuiItem());

        guiController.set(0, SelectGameInfoItem.NORMAL.guiItem);
        guiController.set(9, SelectGameInfoItem.RANK.guiItem, itemBuilder ->
                itemBuilder.formatLore(GeneralConfig.getGameConfig().getRankPlacementPlayCount()));
        guiController.set(17, ButtonItem.LEFT.getGuiItem());

        displayNormalRooms(guiController);
        displayRankRooms(guiController);

    }

    @Override
    public void onClick(InventoryClickEvent event, @NonNull Player player, @NonNull GuiItem<?> guiItem) {
        if (event.getClick() != ClickType.LEFT)
            return;

        if (guiItem.getGui() == null) {
            if (guiItem == ButtonItem.LEFT.getGuiItem())
                player.performCommand("메뉴");

            return;
        }

        if (guiItem.getIdentifier() instanceof SelectGameItem) {
            String[] splittedClickItemName = event.getCurrentItem().getItemMeta().getDisplayName().split(" ");
            int number = Integer.parseInt(splittedClickItemName[2]);
            boolean isRanked = guiItem.getIdentifier() == SelectGameItem.RANK;

            User user = User.fromPlayer(player);
            GameUser gameUser = GameUser.fromUser(user);
            Game game = Game.fromNumber(isRanked, number);
            if (gameUser == null) {
                if (game == null)
                    game = new Game(isRanked, number);
                new GameUser(user, game);
            }

            player.closeInventory();
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

        /** GUI 아이템 객체 */
        private final GuiItem<SelectGameInfoItem> guiItem;

        SelectGameInfoItem(Material material, String name, String... lores) {
            ItemBuilder itemBuilder = new ItemBuilder(material).setName(name);
            for (String lore : lores) {
                itemBuilder.addLore("§f" + lore);
            }

            String[] gamePlayModeNames =
                    Arrays.stream(GamePlayMode.values()).filter(gamePlayMode -> !this.toString().equals("NORMAL"))
                            .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);
            itemBuilder.addLore(gamePlayModeNames);

            this.guiItem = new GuiItem<SelectGameInfoItem>(this, itemBuilder.build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return false;
                }
            };
        }
    }

    private enum SelectGameItem {
        NORMAL(Material.STAINED_GLASS_PANE, ((short) 5), "§a일반 게임 {0}"),
        RANK(Material.STAINED_GLASS_PANE, ((short) 5), "§6랭크 게임 {0}");

        /** GUI 아이템 객체 */
        private final GuiItem<SelectGameItem> guiItem;

        SelectGameItem(Material material, int damage, String name) {
            this.guiItem = new GuiItem<SelectGameItem>(this, new ItemBuilder(material)
                    .setDamage((short) damage)
                    .setName(name)
                    .setLore("§e인원 수 : §f[{0}§f/{1} 명]",
                            "§e상태 : §f{2}",
                            "§e게임 모드 : §f{3}",
                            "§e경과 시간 : §f{4}")
                    .build()) {
                @Override
                public Gui getGui() {
                    return instance;
                }

                @Override
                public boolean isClickable() {
                    return true;
                }
            };
        }
    }
}
