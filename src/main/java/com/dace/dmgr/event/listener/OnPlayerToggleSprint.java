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
import org.bukkit.event.player.PlayerToggleSprintEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerToggleSprint extends EventListener<PlayerToggleSprintEvent> {
    @Getter
    private static final OnPlayerToggleSprint instance = new OnPlayerToggleSprint();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerToggleSprintEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));

        if (combatUser != null && combatUser.getCharacterType() != null)
            combatUser.useAction(ActionKey.SPRINT);
    }
}
