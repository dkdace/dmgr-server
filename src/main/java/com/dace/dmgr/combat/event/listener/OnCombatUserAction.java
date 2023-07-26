package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.SwapModule;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
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
        Action action = combatUser.getActionMap().get(actionKey);
        if (action == null)
            return;

        Weapon weapon = combatUser.getWeapon();
        if (weapon instanceof Swappable && ((Swappable) weapon).getWeaponState() == SwapModule.WeaponState.SECONDARY)
            weapon = ((Swappable) combatUser.getWeapon()).getSubweapon();

        if (action instanceof Weapon) {
            if (!weapon.isCooldownFinished())
                return;

            weapon.onUse(actionKey);
        } else if (action instanceof Skill) {
            if (CooldownManager.getCooldown(combatUser, Cooldown.SILENCE) > 0)
                return;
            if (!((Skill) action).canUse())
                return;
            if (action.getActionInfo() instanceof ActiveSkillInfo) {
                if (!((Skill) action).isGlobalCooldownFinished())
                    return;
                if (weapon instanceof Reloadable)
                    ((Reloadable) weapon).cancelReloading();
            }
            if (action instanceof UltimateSkill && !((Skill) action).isUsing())
                combatUser.useUlt();

            action.onUse(actionKey);
        }
    }
}