package com.dace.dmgr.event.listener;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.character.CharacterType;
import com.dace.dmgr.combat.character.ched.action.ChedWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.user.User;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

public final class OnEntityShootBowEvent implements Listener {
    @EventHandler
    public static void event(EntityShootBowEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        CombatUser combatUser = CombatUser.fromUser(User.fromPlayer((Player) entity));
        if (combatUser == null)
            return;

        event.setCancelled(true);
        if (combatUser.getCharacterType() == CharacterType.CHED) {
            ((ChedWeapon) combatUser.getWeapon()).setPower(event.getForce());
            combatUser.useAction(ActionKey.PERIODIC_1);
        }
    }
}
