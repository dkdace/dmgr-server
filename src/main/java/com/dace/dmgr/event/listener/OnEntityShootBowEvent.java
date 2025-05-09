package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.combatant.ched.ChedWeapon;
import com.dace.dmgr.combat.entity.combatuser.ActionManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.event.EventListener;
import com.dace.dmgr.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnEntityShootBowEvent extends EventListener<EntityShootBowEvent> {
    @Getter
    private static final OnEntityShootBowEvent instance = new OnEntityShootBowEvent();

    @Override
    @EventHandler
    protected void onEvent(@NonNull EntityShootBowEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer((Player) entity));
        if (combatUser == null)
            return;

        event.setCancelled(true);

        if (combatUser.getCombatantType() == CombatantType.CHED) {
            ActionManager actionManager = combatUser.getActionManager();
            ((ChedWeapon) actionManager.getWeapon()).setPower(event.getForce());
            actionManager.useAction(ActionKey.PERIODIC_1);
        }
    }
}
