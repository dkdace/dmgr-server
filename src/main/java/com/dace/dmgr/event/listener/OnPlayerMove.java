package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.FreeCombat;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerMove extends EventListener<PlayerMoveEvent> {
    @Getter
    private static final OnPlayerMove instance = new OnPlayerMove();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerMoveEvent event) {
        Player player = event.getPlayer();
        User user = User.fromPlayer(player);
        CombatUser combatUser = CombatUser.fromUser(user);

        if (combatUser != null && FreeCombat.getInstance().isInFreeCombatWarp(player))
            FreeCombat.getInstance().teleportRandom(player);
    }
}
