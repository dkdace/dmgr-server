package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.Skill;
import com.dace.dmgr.combat.action.Weapon;
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

        if (action instanceof Weapon)
            ((Weapon) action).use(combatUser, combatUser.getWeaponController());
        else if (action instanceof Skill)
            ((Skill) action).use(combatUser, combatUser.getSkillController((Skill) action));

    }
}