package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.TrainingCenter;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.game.GameUser;
import com.dace.dmgr.item.gui.ArenaOption;
import com.dace.dmgr.item.gui.Menu;
import com.dace.dmgr.item.gui.SelectChar;
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

        if (gameUser != null && gameUser.isInSpawn() || FreeCombat.getInstance().isInFreeCombatWait(player)
                || TrainingCenter.getInstance().isInSelectCharZone(player)) {
            new SelectChar(player);
            return;
        }

        CombatUser combatUser = CombatUser.fromUser(user);
        if (combatUser != null) {
            if (TrainingCenter.Arena.getInstance().isInOptionZone(player)) {
                new ArenaOption(player);
                return;
            }

            combatUser.useAction(ActionKey.SWAP_HAND);
            return;
        }

        new Menu(player);
    }
}
