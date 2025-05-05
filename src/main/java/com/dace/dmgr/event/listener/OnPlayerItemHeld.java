package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnPlayerItemHeld extends EventListener<PlayerItemHeldEvent> {
    @Getter
    private static final OnPlayerItemHeld instance = new OnPlayerItemHeld();

    @Override
    @EventHandler
    protected void onEvent(@NonNull PlayerItemHeldEvent event) {
        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer(event.getPlayer()));
        if (combatUser == null)
            return;

        event.setCancelled(true);

        int newSlot = event.getNewSlot();
        if (newSlot >= 4)
            return;

        ActionManager actionManager = combatUser.getActionManager();
        switch (newSlot) {
            case 0:
                actionManager.useAction(ActionKey.SLOT_1);
                break;
            case 1:
                actionManager.useAction(ActionKey.SLOT_2);
                break;
            case 2:
                actionManager.useAction(ActionKey.SLOT_3);
                break;
            case 3:
                actionManager.useAction(ActionKey.SLOT_4);
                break;
            default:
                break;
        }
    }
}
