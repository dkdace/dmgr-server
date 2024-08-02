package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.gui.Menu;
import com.dace.dmgr.item.gui.SelectChar;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class OnPlayerSwapHandItems implements Listener {
    @EventHandler
    public static void event(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);
        CombatUser combatUser = CombatUser.fromUser(user);

        if ((gameUser != null && gameUser.getSpawnRegionTeam() == gameUser.getTeam()) ||
                (combatUser != null && LocationUtil.isInRegion(player, FreeCombat.FREE_COMBAT_REGION))) {
            SelectChar.getInstance().open(player);
            return;
        }
        if (combatUser != null && combatUser.getCharacterType() != null) {
            combatUser.useAction(ActionKey.SWAP_HAND);
            return;
        }

        Menu menu = Menu.getInstance();
        menu.open(player);
    }
}
