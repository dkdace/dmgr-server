package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.item.DefinedItem;
import com.dace.dmgr.user.UserData;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * 코어 목록 GUI 클래스.
 */
public final class CoreList extends ChestGUI {
    /**
     * 코어 목록 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public CoreList(@NonNull Player player) {
        super(6, "§8코어 목록", player);

        set(5, 8, new GUIItem.Previous(Menu::new));

        CharacterType[] characterTypes = CharacterType.sortedValues();
        for (int i = 0; i < characterTypes.length; i++) {
            Set<Core> cores = UserData.fromPlayer(player).getCharacterRecord(characterTypes[i]).getCores();

            set(i, new DefinedItem(characterTypes[i].getProfileItem()), itemBuilder -> {
                itemBuilder.setLore("");

                if (cores.isEmpty())
                    itemBuilder.addLore("§8적용된 코어 없음");
                else
                    for (Core core : cores)
                        itemBuilder.addLore("§b" + core.getName());
            });
        }
    }
}
