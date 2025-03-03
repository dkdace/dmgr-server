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
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerArmorStandManipulate extends EventListener<PlayerArmorStandManipulateEvent> {
    @Getter
    private static final OnPlayerArmorStandManipulate instance = new OnPlayerArmorStandManipulate();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();

        if (!player.isOp())
            event.setCancelled(true);

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(player));
        if (combatUser == null)
            return;

        event.setCancelled(true);
        combatUser.useAction(ActionKey.RIGHT_CLICK);
    }
}
