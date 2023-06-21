package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.ActionInfo;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.skill.SkillInfo;
import com.dace.dmgr.combat.action.skill.UltimateSkillInfo;
import com.dace.dmgr.combat.action.weapon.*;
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
        ActionInfo actionInfo = combatUser.getCharacter().getActionKeyMap().get(actionKey);
        Weapon weapon = combatUser.getWeapon();
        if (weapon.getWeaponState() == WeaponState.SECONDARY)
            weapon = ((Swappable) combatUser.getWeapon()).getSubweapon();

        if (actionInfo instanceof WeaponInfo) {
            if (!weapon.isCooldownFinished())
                return;

            weapon.onUse(actionKey);

        } else if (actionInfo instanceof SkillInfo) {
            Skill skill = combatUser.getSkill((SkillInfo) actionInfo);

            if (CooldownManager.getCooldown(combatUser, Cooldown.SILENCE) > 0)
                return;
            if (skill.getStack() <= 0)
                return;
            if (actionInfo instanceof ActiveSkillInfo) {
                if (!skill.isGlobalCooldownFinished())
                    return;
                if (weapon instanceof Reloadable)
                    ((Reloadable) weapon).cancelReloading();
            }
            if (actionInfo instanceof UltimateSkillInfo && !skill.isUsing())
                combatUser.useUlt();

            skill.onUse(actionKey);
        }
    }
}