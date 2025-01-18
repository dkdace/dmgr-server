package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.gui.Menu;
import com.dace.dmgr.item.gui.SelectChar;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerSwapHandItems extends EventListener<PlayerSwapHandItemsEvent> {
    @Getter
    private static final OnPlayerSwapHandItems instance = new OnPlayerSwapHandItems();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));

        if (combatUser != null) {
            GameUser gameUser = GameUser.fromUser(combatUser.getUser());

            if (gameUser != null && gameUser.getTeam() != null && gameUser.getSpawnRegionTeam() == gameUser.getTeam()
                    || LocationUtil.isInRegion(player, FreeCombat.FREE_COMBAT_REGION)) {
                new SelectChar(player);
                return;
            }
            if (combatUser.getCharacterType() != null) {
                combatUser.useAction(ActionKey.SWAP_HAND);
                return;
            }
        }

        new Menu(player);
    }
}
