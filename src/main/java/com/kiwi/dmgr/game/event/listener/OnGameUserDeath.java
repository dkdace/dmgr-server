package com.kiwi.dmgr.game.event.listener;

import com.dace.dmgr.combat.event.combatuser.CombatUserDeathEvent;
import com.kiwi.dmgr.game.GameUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;
import static com.kiwi.dmgr.game.GameUser.isGameUserEventVaild;

/**
 * 게임 유저가 사망 하였을때 발생하는 이벤트
 */
public class OnGameUserDeath implements Listener {
    @EventHandler
    public void event(CombatUserDeathEvent event) {
        GameUser attacker = gameUserMap.get(event.getAttacker().getEntity().getPlayer());
        GameUser victim = gameUserMap.get(event.getCombatUser().getEntity().getPlayer());
        if (isGameUserEventVaild(attacker, victim)) {
            victim.setKill(victim.getKill() + 1);
        }
    }
}
