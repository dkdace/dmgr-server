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

import java.text.MessageFormat;
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
     * GUI에 게임 방 목록을 표시한다.
     *
     * @param inventory 인벤토리
     */
    private void displayRooms(Inventory inventory) {
        for (int i = 0; i < GameConfig.MAX_ROOM_COUNT; i++) {
            ItemBuilder room;

            Game game = GameInfoRegistry.getGame(false, i);
            if (game == null) {
                room = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDamage((short) 5)
                        .setName(MessageFormat.format(GUI_TEXT.ROOM_NORMAL_NAME, i));
            } else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);

                room = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDamage((short) (game.canJoin() ? 5 : 14))
                        .setName(MessageFormat.format(GUI_TEXT.ROOM_NORMAL_NAME, i))
                        .setLore(MessageFormat.format(GUI_TEXT.ROOM_LORE,
                                (game.getGameUsers().size() >= GameConfig.NORMAL_MIN_PLAYER_COUNT ? "§f" : "§c") + game.getGameUsers().size(),
                                GameConfig.NORMAL_MAX_PLAYER_COUNT,
                                (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime)));
            }

            inventory.setItem(2 + i, room.build());
        }

        for (int i = 0; i < GameConfig.MAX_ROOM_COUNT; i++) {
            ItemBuilder room;

            Game game = GameInfoRegistry.getGame(true, i);
            if (game == null) {
                room = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDamage((short) 5)
                        .setName(MessageFormat.format(GUI_TEXT.ROOM_RANK_NAME, i));
            } else {
                long playTime = System.currentTimeMillis() - game.getStartTime();
                String displayTime = DurationFormatUtils.formatDuration(playTime, "mm:ss", true);

                room = new ItemBuilder(Material.STAINED_GLASS_PANE)
                        .setDamage((short) (game.canJoin() ? 5 : 14))
                        .setName(MessageFormat.format(GUI_TEXT.ROOM_RANK_NAME, i))
                        .setLore(MessageFormat.format(GUI_TEXT.ROOM_LORE,
                                (game.getGameUsers().size() >= GameConfig.RANK_MIN_PLAYER_COUNT ? "§f" : "§c") + game.getGameUsers().size(),
                                GameConfig.RANK_MAX_PLAYER_COUNT,
                                (game.getPhase() == Game.Phase.WAITING ? "§a" : "§c") + game.getPhase().getName(),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : game.getGamePlayMode().getName()),
                                (game.getPhase() == Game.Phase.WAITING ? "§8--" : displayTime)));
            }

            inventory.setItem(11 + i, room.build());
        }
    }

    @Override
    protected void onOpen(Player player, Inventory inventory) {
        InventoryUtil.fillAll(inventory, DisplayItem.EMPTY.getItemStack());

        String[] normalGamePlayModeNames =
                Arrays.stream(GameInfoRegistry.getGamePlayModes(false))
                        .map(gamePlayMode -> MessageFormat.format(GUI_TEXT.LORE_MODES, gamePlayMode.getName())).toArray(String[]::new);
        String[] rankGamePlayModeNames =
                Arrays.stream(GameInfoRegistry.getGamePlayModes(true))
                        .map(gamePlayMode -> MessageFormat.format(GUI_TEXT.LORE_MODES, gamePlayMode.getName())).toArray(String[]::new);

        inventory.setItem(0,
                new ItemBuilder(Material.IRON_SWORD)
                        .setName(GUI_TEXT.NORMAL_NAME)
                        .setLore(GUI_TEXT.NORMAL_LORE)
                        .addLore(normalGamePlayModeNames)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
        inventory.setItem(9,
                new ItemBuilder(Material.DIAMOND_SWORD)
                        .setName(GUI_TEXT.RANK_NAME)
                        .setLore(MessageFormat.format(GUI_TEXT.RANK_LORE, GameConfig.RANK_PLACEMENT_PLAY_COUNT))
                        .addLore(rankGamePlayModeNames)
                        .addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());

        displayRooms(inventory);
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

    /**
     * GUI에 사용되는 텍스트.
     */
    private interface GUI_TEXT {
        /** 일반 게임 이름 */
        String NORMAL_NAME = "§a§l일반";
        /** 일반 게임 설명 */
        String NORMAL_LORE = "§f랭크 점수에 반영되지 않는 일반 게임입니다." +
                "\n" +
                "\n§f다음 게임 모드 중에서 무작위로 선택됨 :";
        /** 랭크 게임 이름 */
        String RANK_NAME = "§6§l랭크";
        /** 랭크 게임 설명 */
        String RANK_LORE = "§f랭크 점수에 반영되는 랭크 게임입니다." +
                "\n§f랭크 게임 §e{0}판§f을 플레이하면 첫 티어 및 랭크 점수가 결정됩니다." +
                "\n§f중간 난입이 불가능한 게임입니다." +
                "\n" +
                "\n§f다음 게임 모드 중에서 무작위로 선택됨 :";
        /** 게임 설명의 모드 목록 */
        String LORE_MODES = "§e- {0}";
        /** 일반 게임 방 이름 */
        String ROOM_NORMAL_NAME = "§a일반 게임 {0}";
        /** 랭크 게임 방 이름 */
        String ROOM_RANK_NAME = "§6랭크 게임 {0}";
        /** 게임 방 설명 */
        String ROOM_LORE = "§e인원 수 : §f[{0}§f/{1} 명]" +
                "\n§e상태 : §f{2}" +
                "\n§e게임 모드 : §f{3}" +
                "\n§e경과 시간 : §f{4}";
    }
}
