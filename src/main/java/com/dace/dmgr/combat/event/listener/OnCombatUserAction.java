package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.*;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnCombatUserAction implements Listener {
    @EventHandler
    public static void event(CombatUserActionEvent event) {
        CombatUser combatUser = event.getCombatUser();
        ActionKey actionKey = event.getActionKey();
        Action action = combatUser.getCharacter().getActionKeyMap().get(actionKey);
        WeaponController weaponController = combatUser.getWeaponController();

        if (action instanceof Weapon) {
            if (!weaponController.isCooldownFinished())
                return;

            ((Weapon) action).use(combatUser, weaponController, actionKey);

        } else if (action instanceof Skill) {
            SkillController skillController = combatUser.getSkillController((Skill) action);

            if (CooldownManager.getCooldown(combatUser, Cooldown.SILENCE) > 0)
                return;
            if (!skillController.isCooldownFinished())
                return;
            if (action instanceof ActiveSkill) {
                if (!skillController.isGlobalCooldownFinished())
                    return;
                weaponController.setReloading(false);
            }
            if (action instanceof UltimateSkill && !skillController.isUsing())
                combatUser.useUlt();

            ((Skill) action).use(combatUser, skillController, actionKey);
        }
    }
}