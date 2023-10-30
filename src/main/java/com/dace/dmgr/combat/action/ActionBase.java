package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.action.skill.SkillBase;
import com.dace.dmgr.combat.action.weapon.WeaponBase;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.task.TaskManager;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

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
    /** 모듈 목록 */
    private ActionModule[] modules = new ActionModule[0];

    /**
     * 동작 인스턴스를 생성한다.
     *
     * <p>{@link ActionBase#init()}을 호출하여 초기화해야 한다. </p>
     *
     * @param combatUser 대상 플레이어
     * @param actionInfo 동작 정보
     */
    protected ActionBase(CombatUser combatUser, ActionInfo actionInfo) {
        this.combatUser = combatUser;
        this.actionInfo = actionInfo;
        this.itemStack = actionInfo.getItemStack().clone();
    }

    @Override
    @MustBeInvokedByOverriders
    public void init() {
        modules = getModules();
    }

    @Override
    public String getTaskIdentifier() {
        return toString();
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
            onCooldownSet();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
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
        TaskManager.addTask(this, new TaskTimer(1) {
            @Override
            public boolean onTimerTick(int i) {
                onCooldownTick();

                if (isCooldownFinished()) {
                    onCooldownFinished();
                    return false;
                }

                return true;
            }
        });
    }

    /**
     * 쿨타임을 설정했을 때 실행할 작업.
     */
    protected abstract void onCooldownSet();

    /**
     * 쿨타임이 진행할 때 (매 tick마다) 실행할 작업.
     */
    protected abstract void onCooldownTick();

    /**
     * 쿨타임이 끝났을 때 실행할 작업.
     */
    protected abstract void onCooldownFinished();

    @Override
    public final boolean isCooldownFinished() {
        return getCooldown() == 0;
    }

    @Override
    public boolean canUse() {
        return isCooldownFinished();
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        setCooldown(getDefaultCooldown());
        for (ActionModule module : modules) {
            module.onReset();
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void remove() {
        TaskManager.clearTask(this);
        for (ActionModule module : modules) {
            module.onRemove();
        }
    }
}
