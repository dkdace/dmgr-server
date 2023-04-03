package com.kiwi.dmgr.game.event.listener;

import com.dace.dmgr.combat.event.combatuser.CombatUserHealEvent;
import com.kiwi.dmgr.game.GameUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;
import static com.kiwi.dmgr.game.GameUser.isGameUserEventVaild;

/**
 * 게임 유저가 치유 하였을때 발생하는 이벤트
 */
public class OnGameUserHeal implements Listener {
    @EventHandler
    public void event(CombatUserHealEvent event) {
        GameUser attacker = gameUserMap.get(event.getCombatUser().getEntity().getPlayer());
        GameUser victim = gameUserMap.get(event.getVictim().getEntity().getPlayer());
        int heal = event.getHeal();
        if (isGameUserEventVaild(attacker, victim)) {
            attacker.setHeal(attacker.getHeal() + heal);
        }
    }
}
