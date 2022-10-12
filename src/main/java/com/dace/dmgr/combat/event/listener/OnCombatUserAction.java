package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnCombatUserAction implements Listener {
    @EventHandler
    public static void event(CombatUserActionEvent event) {
        CombatUser combatUser = event.getCombatUser();
        ActionKey actionKey = event.getActionKey();
        Action action = combatUser.getCharacter().getActionKeyMap().get(actionKey);

        if (action instanceof Weapon) {
            WeaponController weaponController = combatUser.getWeaponController();
            if (weaponController.getRemainingAmmo() > 0)
                ((Weapon) action).use(combatUser, weaponController);
        } else if (action instanceof Skill)
            ((Skill) action).use(combatUser, combatUser.getSkillController((Skill) action));
    }
}