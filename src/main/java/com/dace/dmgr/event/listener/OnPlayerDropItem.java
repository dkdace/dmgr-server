package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerDropItem extends EventListener<PlayerDropItemEvent> {
    @Getter
    private static final OnPlayerDropItem instance = new OnPlayerDropItem();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return;

        event.setCancelled(true);
        combatUser.useAction(ActionKey.DROP);
    }
}
