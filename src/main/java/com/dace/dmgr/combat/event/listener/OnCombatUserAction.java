package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.SwapModule;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class OnCombatUserAction implements Listener {
    @EventHandler
    public static void event(CombatUserActionEvent event) {
        CombatUser combatUser = event.getCombatUser();
        ActionKey actionKey = event.getActionKey();
        Action action = combatUser.getActionMap().get(actionKey);
        if (action == null)
            return;

        Weapon weapon = combatUser.getWeapon();
        if (weapon instanceof Swappable && ((Swappable) weapon).getWeaponState() == SwapModule.WeaponState.SECONDARY)
            weapon = ((Swappable) combatUser.getWeapon()).getSubweapon();

        if (action instanceof Weapon) {
            if (!weapon.canUse())
                return;

            weapon.onUse(actionKey);
        } else if (action instanceof Skill) {
            if (!action.canUse())
                return;
            if (combatUser.hasStatusEffect(StatusEffectType.SILENCE))
                return;
            if (action.getActionInfo() instanceof ActiveSkillInfo && weapon instanceof Reloadable)
                ((Reloadable) weapon).cancelReloading();

            action.onUse(actionKey);
        }
    }
}