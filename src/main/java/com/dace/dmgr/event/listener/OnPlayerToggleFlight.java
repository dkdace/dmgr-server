package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerToggleFlight extends EventListener<PlayerToggleFlightEvent> {
    @Getter
    private static final OnPlayerToggleFlight instance = new OnPlayerToggleFlight();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerToggleFlightEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));

        if (combatUser != null)
            combatUser.useAction(ActionKey.SPACE);
    }
}
