package com.dace.dmgr.gui;

import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameConfig;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.gui.item.DisplayItem;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.GameInfoRegistry;
import com.dace.dmgr.util.InventoryUtil;
import lombok.Getter;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;

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

    @Override
    protected void onOpen(Player player, Inventory inventory) {
        InventoryUtil.fillAll(inventory, DisplayItem.EMPTY.getItemStack());

        String[] normalGamePlayModeNames =
                Arrays.stream(GameInfoRegistry.getGamePlayModes(false))
                        .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);
        String[] rankGamePlayModeNames =
                Arrays.stream(GameInfoRegistry.getGamePlayModes(true))
                        .map(gamePlayMode -> "§e- " + gamePlayMode.getName()).toArray(String[]::new);

        inventory.setItem(0,
                new ItemBuilder(Material.IRON_SWORD)
                        .setName("§a§l일반")
                        .setLore("§f랭크 점수에 반영되지 않는 일반 게임입니다.",
                                "",
                                "§f다음 게임 모드 중에서 무작위로 선택됨 :")
                        .addLore(normalGamePlayModeNames)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
        inventory.setItem(9,
                new ItemBuilder(Material.DIAMOND_SWORD)
                        .setName("§6§l랭크")
                        .setLore("§f랭크 점수에 반영되는 랭크 게임입니다.",
                                "§f랭크 게임 §e" + GameConfig.RANK_PLACEMENT_PLAY_COUNT + "판§f을 플레이하면 첫 티어 및 랭크 점수가 결정됩니다.",
                                "§f중간 난입이 불가능한 게임입니다.",
                                "",
                                "§f다음 게임 모드 중에서 무작위로 선택됨 :")
                        .addLore(rankGamePlayModeNames)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());

        for (int i = 0; i < GameConfig.MAX_ROOM_COUNT; i++) {
            ItemBuilder room;

            Game game = GameInfoRegistry.getGame(false, i);
            if (game == null) {
                room = new ItemBuilder(DisplayItem.ENABLED.getItemStack())
                        .setName("§a일반 게임 " + i)
                        .setLore("§7비어 있음");
            } else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);

                room = new ItemBuilder(game.canJoin() ? DisplayItem.ENABLED.getItemStack() : DisplayItem.DISABLED.getItemStack())
                        .setName("§a일반 게임 " + i)
                        .setLore("§3인원 수 : §f[" + (game.getGameUsers().size() >= GameConfig.NORMAL_MIN_PLAYER_COUNT ? "§f" : "§c") +
                                        game.getGameUsers().size() + "§f/" + GameConfig.NORMAL_MAX_PLAYER_COUNT + " 명]",
                                "§3상태 : " + (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                "§3게임 모드 : §f" + (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                "§3경과 시간 : §f" + (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime));
            }

            inventory.setItem(1 + i, room.build());
        }

        for (int i = 0; i < GameConfig.MAX_ROOM_COUNT; i++) {
            ItemBuilder room;

            Game game = GameInfoRegistry.getGame(true, i);
            if (game == null) {
                room = new ItemBuilder(DisplayItem.ENABLED.getItemStack())
                        .setName("§6랭크 게임 " + i)
                        .setLore("§7비어 있음");
            } else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);

                room = new ItemBuilder(game.canJoin() ? DisplayItem.ENABLED.getItemStack() : DisplayItem.DISABLED.getItemStack())
                        .setName("§6랭크 게임 " + i)
                        .setLore("§3인원 수 : §f[" + (game.getGameUsers().size() >= GameConfig.RANK_MIN_PLAYER_COUNT ? "§f" : "§c") +
                                        game.getGameUsers().size() + "§f/" + GameConfig.RANK_MAX_PLAYER_COUNT + " 명]",
                                "§3상태 : " + (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                "§3게임 모드 : §f" + (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                "§3경과 시간 : §f" + (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime));
            }

            inventory.setItem(10 + i, room.build());
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event, Player player, String clickItemName) {
        if (!clickItemName.contains("게임"))
            return;

        if (event.getClick() == ClickType.LEFT) {
            String[] splittedClickItemName = clickItemName.split(" ");
            int number = Integer.parseInt(splittedClickItemName[2]);
            boolean isRanked = splittedClickItemName[0].equals("랭크");

            GameUser gameUser = EntityInfoRegistry.getGameUser(player);
            Game game = GameInfoRegistry.getGame(isRanked, number);
            if (gameUser == null) {
                if (game == null) {
                    game = new Game(isRanked, number);
                    game.init();
                }
                game.addPlayer(player);
            }

            player.closeInventory();
        }
    }
}
