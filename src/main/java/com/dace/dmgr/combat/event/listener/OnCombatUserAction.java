package com.dace.dmgr.combat.event.listener;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.action.skill.SkillBase;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.event.combatuser.CombatUserActionEvent;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskTimer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class OnCombatUserAction implements Listener {
    @EventHandler
    public static void event(CombatUserActionEvent event) {
        CombatUser combatUser = event.getCombatUser();
        ActionKey actionKey = event.getActionKey();
        Action action = combatUser.getActionMap().get(actionKey);
        if (combatUser.isDead() || action == null)
            return;

        Weapon weapon = combatUser.getWeapon();
        if (weapon instanceof Swappable && ((Swappable<?>) weapon).getSwapState() == Swappable.SwapState.SECONDARY)
            weapon = ((Swappable<?>) combatUser.getWeapon()).getSubweapon();

        if (action instanceof WeaponBase) {
            if (weapon instanceof FullAuto && (((FullAuto) weapon).getKey() == actionKey))
                handleUseFullAutoWeapon(weapon, actionKey, combatUser);
            else
                handleUseWeapon(weapon, actionKey);

        } else if (action instanceof SkillBase) {
            if (!action.canUse())
                return;
            if (combatUser.hasStatusEffect(StatusEffectType.SILENCE))
                return;
            if (action.getActionInfo() instanceof ActiveSkillInfo && weapon instanceof Reloadable)
                ((Reloadable) weapon).setReloading(false);

            action.onUse(actionKey);
        }
    }

    private static void handleUseWeapon(Weapon weapon, ActionKey actionKey) {
        if (!weapon.canUse())
            return;

        weapon.onUse(actionKey);
    }

    private static void handleUseFullAutoWeapon(Weapon weapon, ActionKey actionKey, CombatUser combatUser) {
        if (CooldownManager.getCooldown(weapon, Cooldown.WEAPON_FULLAUTO_COOLDOWN) > 0)
            return;

        CooldownManager.setCooldown(weapon, Cooldown.WEAPON_FULLAUTO_COOLDOWN);

        new TaskTimer(1, 4) {
            int j = 0;

            @Override
            public boolean run(int i) {
                if (j > 0 && weapon instanceof Reloadable && ((Reloadable) weapon).isReloading())
                    return true;
                if (weapon.canUse() && FullAuto.isFireTick(((FullAuto) weapon).getFireRate(), combatUser.getEntity().getTicksLived())) {
                    j++;
                    weapon.onUse(actionKey);
                }

                return true;
            }
        };
    }
}