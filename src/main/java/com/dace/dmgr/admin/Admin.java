package com.dace.dmgr.admin;

import com.dace.dmgr.combat.CombatTick;
import com.dace.dmgr.combat.character.arkace.Arkace;
import com.dace.dmgr.combat.entity.CombatUser;
import org.bukkit.entity.Player;

/**
 * 플러그인 테스트용 어드민 클래스.
 */
public class Admin {
    /**
     * 플레이어의 전투원을 설정한다.
     *
     * @param player    대상 플레이어
     * @param team      팀
     * @param character 전투원
     */
    public static void selectCharacter(Player player, String team, String character) {
        CombatUser combatUser = new CombatUser(player);

        combatUser.setTeam(team);
        combatUser.setCharacter(Arkace.getInstance());
        CombatTick.run(combatUser);
    }
}
