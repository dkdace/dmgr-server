package com.dace.dmgr.combat.action;

import com.dace.dmgr.combat.action.info.ActionInfo;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Action}의 기본 구현체, 모든 동작(무기, 스킬)의 기반 클래스.
 *
 * @see AbstractWeapon
 * @see AbstractSkill
 */
public abstract class AbstractAction implements Action {
    /** 플레이어 객체 */
    @Getter
    protected final CombatUser combatUser;
    /** 동작 정보 객체 */
    @Getter
    protected final ActionInfo actionInfo;

    protected final Object taskRunner = new Object();
    /** 아이템 */
    @Getter
    protected ItemStack itemStack;
    /** 비활성화 여부 */
    @Getter
    private boolean isDisposed = false;

    /**
     * 동작 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param actionInfo 동작 정보
     */
    protected AbstractAction(@NonNull CombatUser combatUser, @NonNull ActionInfo actionInfo) {
        this.combatUser = combatUser;
        this.actionInfo = actionInfo;
        this.itemStack = actionInfo.getItemStack().clone();
    }

    @Override
    public final long getCooldown() {
        return CooldownUtil.getCooldown(this, Cooldown.SKILL_COOLDOWN);
    }

    @Override
    public final void setCooldown(long cooldown) {
        if (isCooldownFinished()) {
            CooldownUtil.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
            runCooldown();
            onCooldownSet();
        } else
            CooldownUtil.setCooldown(this, Cooldown.SKILL_COOLDOWN, cooldown);
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
        TaskUtil.addTask(this, new IntervalTask(i -> {
            onCooldownTick();

            if (isCooldownFinished()) {
                onCooldownFinished();
                return false;
            }

            return true;
        }, 1));
    }

    /**
     * 쿨타임을 설정했을 때 실행할 작업.
     */
    protected void onCooldownSet() {
        // 미사용
    }

    /**
     * 쿨타임이 진행할 때 (매 tick마다) 실행할 작업.
     */
    protected void onCooldownTick() {
        // 미사용
    }

    /**
     * 쿨타임이 끝났을 때 실행할 작업.
     */
    protected void onCooldownFinished() {
        // 미사용
    }

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
    public void onCancelled() {
        TaskUtil.clearTask(taskRunner);
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        setCooldown(getDefaultCooldown());
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        checkAccess();

        reset();
        TaskUtil.clearTask(taskRunner);
        TaskUtil.clearTask(this);
        isDisposed = true;
    }
}
