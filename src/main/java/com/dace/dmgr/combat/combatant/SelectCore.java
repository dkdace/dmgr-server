package com.dace.dmgr.combat.combatant;

import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.item.ChestGUI;
import com.dace.dmgr.user.User;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;

/**
 * 코어 선택 GUI 클래스.
 */
public final class SelectCore extends ChestGUI {
    /**
     * 코어 선택 GUI 인스턴스를 생성한다.
     *
     * @param player GUI 표시 대상 플레이어
     */
    public SelectCore(@NonNull Player player) {
        super(3, "§c§l코어 선택", player);

        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser == null)
            return;

        Set<Core> cores = user.getUserData().getCombatantRecord(combatUser.getCombatantType()).getCores();

        int i = 0;
        for (Iterator<Core> iterator = cores.iterator(); iterator.hasNext(); i++) {
            Core core = iterator.next();
            if (!combatUser.hasCore(core))
                set(i, core.getSelectItem());
        }
    }
}
