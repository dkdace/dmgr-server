package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.action.skill.SkillBase;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * 모든 동작(무기, 스킬)의 기반 클래스.
 *
 * @see WeaponBase
 * @see SkillBase
 */
@Getter
public abstract class ActionBase implements Action {
    /** 플레이어 객체 */
    protected final CombatUser combatUser;
    /** 동작 정보 객체 */
    protected final ActionInfo actionInfo;
    /** 아이템 */
    protected ItemStack itemStack;

    protected ActionBase(CombatUser combatUser, ActionInfo actionInfo) {
        this.combatUser = combatUser;
        this.actionInfo = actionInfo;
        this.itemStack = actionInfo.getItemStack().clone();
    }

    @Override
    public final long getCooldown() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_COOLDOWN);
    }

    @Override
    public final void setCooldown(long cooldown) {
        if (isCooldownFinished()) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
            runCooldown();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);

        onCooldownSet();
    }

    @Override
    public final void setCooldown() {
        setCooldown(getDefaultCooldown());
    }

    @Override
    public final void addCooldown(long cooldown) {
        setCooldown(getCooldown() + cooldown);
    }

    /**
     * 쿨타임 스케쥴러를 실행한다.
     */
    private void runCooldown() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onCooldownTick();

                if (isCooldownFinished()) {
                    onCooldownFinished();
                    return false;
                }

                return true;
            }
        };
    }

    @Override
    public final boolean isCooldownFinished() {
        return getCooldown() == 0;
    }

    @Override
    public boolean canUse() {
        return isCooldownFinished();
    }
}
