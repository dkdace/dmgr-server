package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.item.StaticItem;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 전적 GUI 클래스.
 */
public final class Stat extends Gui {
    /** 플레이어 전적 GUI 아이템 객체 */
    private static final StaticItem playerInto = new StaticItem("StatPlayer", new ItemBuilder(Material.SKULL_ITEM)
            .setDamage((short) 3)
            .setLore("",
                    "§e승률 : §b{0}승 §f/ §c{1}패 §f({2}%)",
                    "§e탈주 : §c{3}회 §f({4}%)",
                    "§e플레이 시간 : §f{5}")
            .build());
    /** 이전 버튼 GUI 아이템 객체 */
    private static final GuiItem buttonLeft = new ButtonItem.Left("StatLeft") {
        @Override
        public boolean onClick(@NonNull ClickType clickType, @NonNull ItemStack clickItem, @NonNull Player player) {
            player.performCommand("메뉴");
            return true;
        }
    };

    /** 대상 플레이어 데이터 정보 */
    private final UserData userData;

    /**
     * 지정한 플레이어의 전적 GUI 인스턴스를 생성한다.
     *
     * @param userData 대상 플레이어 데이터 정보
     */
    public Stat(@NonNull UserData userData) {
        super(6, "§8전적");
        this.userData = userData;
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        guiController.fillRow(1, DisplayItem.EMPTY.getStaticItem());

        new AsyncTask<Void>((onFinish, onError) ->
                guiController.set(4, playerInto, itemBuilder -> {
                    itemBuilder.setName(userData.getDisplayName()).setSkullOwner(Bukkit.getOfflinePlayer(userData.getPlayerUUID()));
                    itemBuilder.formatLore(userData.getWinCount(), userData.getLoseCount(),
                            (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100,
                            userData.getQuitCount(),
                            (double) userData.getQuitCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100,
                            DurationFormatUtils.formatDuration(userData.getPlayTime().toMilliseconds(), "d일 H시간 m분"), "");
                })
        );

        if (player.getName().equals(userData.getPlayerName()))
            guiController.set(8, buttonLeft);

        CharacterType[] characterTypes = Arrays.stream(CharacterType.values())
                .sorted(Comparator.comparing(characterType -> characterType.getCharacter().getName()))
                .toArray(CharacterType[]::new);
        for (int i = 0; i < characterTypes.length; i++) {
            UserData.CharacterRecord characterRecord = userData.getCharacterRecord(characterTypes[i]);

            guiController.set(i + 9, CharacterType.valueOf(characterTypes[i].toString()).getGuiItem(), itemBuilder ->
                    itemBuilder.setLore("",
                            MessageFormat.format("§e킬/데스 : §b{0} §f/ §c{1} §f({2})",
                                    characterRecord.getKill(), characterRecord.getDeath(),
                                    (double) characterRecord.getKill() / (characterRecord.getDeath() == 0 ? 1 : characterRecord.getDeath())),
                            MessageFormat.format("§e플레이 시간 : §f{0}",
                                    DurationFormatUtils.formatDuration(characterRecord.getPlayTime().toMilliseconds(), "d일 H시간 m분"))));
        }
    }
}
