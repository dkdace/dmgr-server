package com.dace.dmgr.item.gui;

import com.dace.dmgr.combat.Core;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * 코어 선택 GUI 클래스.
 */
public final class SelectCore extends Gui {
    @Getter
    private static final SelectCore instance = new SelectCore();

    private SelectCore() {
        super(3, "§c§l코어 선택");
    }

    @Override
    public void onOpen(@NonNull Player player, @NonNull GuiController guiController) {
        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser == null || combatUser.getCharacterType() == null)
            return;

        Set<Core> cores = user.getUserData().getCharacterRecord(combatUser.getCharacterType()).getCores();

        int i = 0;
        for (Core core : cores) {
            if (!combatUser.hasCore(core))
                guiController.set(i, core.getSelectGuiItem());
            i++;
        }
    }
}
