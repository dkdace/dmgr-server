package com.dace.dmgr.util;

import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

public class Admin {
    public static void selectCharacter(Player player, String team, String character) {
        CombatUser combatUser = new CombatUser(player);
        combatUserMap.put(player, combatUser);

        combatUser.setTeam(team);
        combatUser.setCharacter(Arkace.getInstance());
        CombatTick.run(combatUser);
    }
}
