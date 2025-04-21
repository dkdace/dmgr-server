package com.dace.dmgr.menu;

import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.item.GUIItem;
import com.dace.dmgr.item.ItemBuilder;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

/**
 * 전적 GUI 클래스.
 */
public final class Stat extends ChestGUI {
    /**
     * 지정한 플레이어 데이터에 해당하는 전적 GUI 인스턴스를 생성한다.
     *
     * @param player   GUI 표시 대상 플레이어
     * @param userData 대상 플레이어 데이터 정보
     */
    public Stat(@NonNull Player player, @NonNull UserData userData) {
        super(6, "§8전적", player);

        fillRow(0, GUIItem.EMPTY);

        new AsyncTask<>((onFinish, onError) ->
                set(0, 4, new DefinedItem(new ItemBuilder(userData.getProfileItem())
                        .setLore("",
                                "§e승률 : §b{0}승 §f/ §c{1}패 §f({2}%)",
                                "§e탈주 : §c{3}회",
                                "§e플레이 시간 : §f{4}")
                        .formatLore(
                                userData.getWinCount(),
                                userData.getLoseCount(),
                                (double) userData.getWinCount() / (userData.getNormalPlayCount() + userData.getRankPlayCount()) * 100,
                                userData.getQuitCount(),
                                DurationFormatUtils.formatDuration(userData.getPlayTime().toMilliseconds(), "d일 H시간 m분"), "")
                        .build())));

        displayCombatantStats(userData);

        if (UserData.fromPlayer(player) == userData)
            set(0, 8, new GUIItem.Previous(Menu::new));
    }

    /**
     * 모든 전투원별 전적 정보를 표시한다.
     *
     * @param userData 대상 플레이어 데이터 정보
     */
    private void displayCombatantStats(@NonNull UserData userData) {
        CombatantType[] combatantTypes = CombatantType.sortedValues();
        for (int i = 0; i < combatantTypes.length; i++) {
            UserData.CombatantRecord combatantRecord = userData.getCombatantRecord(combatantTypes[i]);

            set(i + 9, new DefinedItem(combatantTypes[i].getProfileItem()), itemBuilder -> itemBuilder
                    .setLore("",
                            "§e킬/데스 : §b{0} §f/ §c{1} §f({2})",
                            "§e플레이 시간 : §f{3}")
                    .formatLore(
                            combatantRecord.getKill(),
                            combatantRecord.getDeath(),
                            (double) combatantRecord.getKill() / (combatantRecord.getDeath() == 0 ? 1 : combatantRecord.getDeath()),
                            DurationFormatUtils.formatDuration(combatantRecord.getPlayTime().toMilliseconds(), "d일 H시간 m분")));
        }
    }
}
