package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class OnPlayerInteract implements Listener {
    @EventHandler
    public static void event(PlayerInteractEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));
        if (combatUser == null)
            return;

        if (event.hasBlock() && (LocationUtil.isInteractable(event.getClickedBlock()) || event.getAction() == Action.LEFT_CLICK_BLOCK))
            event.setCancelled(true);
        if (combatUser.getCharacterType() == null)
            return;

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                combatUser.useAction(ActionKey.LEFT_CLICK);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                combatUser.useAction(ActionKey.RIGHT_CLICK);
                break;
            default:
                break;
        }
    }
}
