package com.kiwi.dmgr.game.event.listener;

import com.dace.dmgr.combat.event.combatuser.CombatUserAssistEvent;
import com.kiwi.dmgr.game.GameUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;
import static com.kiwi.dmgr.game.GameUtil.isGameUserEventVaild;

/**
 * 게임 유저가 어시스트 하였을때 발생하는 이벤트
 */
public class OnGameUserAssist implements Listener {
    @EventHandler
    public void event(CombatUserAssistEvent event) {
        GameUser attacker = gameUserMap.get(event.getCombatUser().getEntity().getPlayer());
        GameUser victim = gameUserMap.get(event.getVictim().getEntity().getPlayer());
        if (isGameUserEventVaild(attacker, victim)) {
            attacker.setAssist(attacker.getAssist() + 1);
        }
    }
}
