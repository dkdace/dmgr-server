package com.kiwi.dmgr.game.event.listener;

import com.dace.dmgr.combat.event.combatuser.CombatUserRespawnEvent;
import com.dace.dmgr.lobby.Lobby;
import com.kiwi.dmgr.game.Game;
import com.kiwi.dmgr.game.GameUser;
import com.kiwi.dmgr.game.Team;
import com.kiwi.dmgr.game.map.Point;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.kiwi.dmgr.game.GameMapList.gameUserMap;

/**
 * 게임 유저가 리스폰 하였을때 발생하는 이벤트
 */
public class OnGameUserRespawn implements Listener {
    @EventHandler
    public void event(CombatUserRespawnEvent event) {
        GameUser victim = gameUserMap.get(event.getCombatUser().getEntity().getPlayer());
        Game game = victim.getGame();
        Team team = victim.getTeam();
        event.getCombatUser().getEntity().teleport(game.getPointLocation(team));
    }
}
