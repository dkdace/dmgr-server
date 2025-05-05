package com.dace.dmgr.event.listener;

import com.dace.dmgr.GeneralConfig;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.combatant.SelectChar;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.trainingcenter.ArenaOption;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.menu.Menu;
import com.dace.dmgr.user.User;
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
        User user = User.fromPlayer(player);
        GameUser gameUser = GameUser.fromUser(user);

        if (gameUser != null && gameUser.isInSpawn() || GeneralConfig.getFreeCombatConfig().getWaitRegion().isIn(player)
                || GeneralConfig.getTrainingConfig().getSelectCharRegion().isIn(player)) {
            new SelectChar(player);
            return;
        }

        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser != null) {
            if (GeneralConfig.getTrainingConfig().getArenaConfig().getOptionRegion().isIn(player)) {
                new ArenaOption(player);
                return;
            }

            combatUser.getActionManager().useAction(ActionKey.SWAP_HAND);
            return;
        }

        new Menu(player);
    }
}
