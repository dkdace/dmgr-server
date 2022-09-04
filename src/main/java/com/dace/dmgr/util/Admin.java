package com.dace.dmgr.util;

import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.character.arkace.Arkace;
import org.bukkit.entity.Player;

public class Admin {
    public static void selectCharacter(Player player, String team, String character) {
        CombatUser combatUser = new CombatUser(player);
        combatUser.setTeam(team);
        combatUser.setCharacter(Arkace.getInstance());
        CombatTick.run(combatUser);
    }
}