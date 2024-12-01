package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 여러 번 사용할 수 있는 스택형 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class StackableSkill extends ActiveSkill {
    /** 스킬 스택 충전 쿨타임 ID */
    private static final String SKILL_STACK_COOLDOWN_ID = "SkillStackCooldown";
    /** 스킬 스택 수 */
    private int stack = 0;

    /**
     * @see ActiveSkill#ActiveSkill(CombatUser, ActiveSkillInfo, int)
     */
    protected StackableSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<? extends ActiveSkill> activeSkillInfo, int slot) {
        super(combatUser, activeSkillInfo, slot);
        setStackCooldown(getDefaultStackCooldown());
    }

    @Override
    protected void onTick() {
        if (stack > 0) {
            if (isDurationFinished())
                displayReady(stack);
            else
                displayUsing(stack);
        } else
            displayCooldown(1);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && stack > 0;
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        super.reset();

        setStackCooldown(0);
        addStack(getMaxStack());
    }

    /**
     * 기본 스택 충전 쿨타임을 반환한다.
     *
     * @return 스택 충전 쿨타임 (tick)
     */
    public abstract long getDefaultStackCooldown();

    /**
     * 스킬의 남은 스택 충전 쿨타임을 반환한다.
     *
     * @return 스택 충전 쿨타임 (tick)
     */
    public final long getStackCooldown() {
        return CooldownUtil.getCooldown(this, SKILL_STACK_COOLDOWN_ID);
    }

    /**
     * 스킬의 스택 충전 쿨타임을 설정한다.
     *
     * @param cooldown 스택 충전 쿨타임 (tick). -1로 설정 시 무한 지속
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public final void setStackCooldown(long cooldown) {
        if (cooldown < -1)
            throw new IllegalArgumentException("'cooldown'이 -1 이상이어야 함");
        if (stack >= getMaxStack())
            return;

        if (isStackCooldownFinished()) {
            CooldownUtil.setCooldown(this, SKILL_STACK_COOLDOWN_ID, cooldown);
            runStackCooldown(cooldown);
        } else
            CooldownUtil.setCooldown(this, SKILL_STACK_COOLDOWN_ID, cooldown);
    }

    /**
     * 스킬의 스택 충전 쿨타임 스케쥴러를 실행한다.
     *
     * @param cooldown 스택 충전 쿨타임 (tick). -1로 설정 시 무한 지속
     */
    private void runStackCooldown(long cooldown) {
        TaskUtil.addTask(this, new IntervalTask(i -> {
            if (isStackCooldownFinished()) {
                onStackCooldownFinished();

                addStack(1);
                if (stack < getMaxStack())
                    setStackCooldown(cooldown);

                return false;
            }

            return true;
        }, 1));
    }

    /**
     * 스택 충전 쿨타임이 끝났을 때 실행할 작업.
     */
    @MustBeInvokedByOverriders
    protected void onStackCooldownFinished() {
        READY_SOUND.play(combatUser.getEntity());
    }

    /**
     * 최대 스택 충전량을 반환한다.
     *
     * @return 최대 스택 충전량
     */
    public abstract int getMaxStack();

    /**
     * 지정한 양만큼 스킬의 스택 수를 증가시킨다.
     *
     * @param amount 스택 증가량
     */
    public final void addStack(int amount) {
        stack = Math.min(Math.max(0, stack + amount), getMaxStack());

        if (stack > 0 && isStackCooldownFinished())
            setStackCooldown(getDefaultStackCooldown());
    }

    /**
     * 스킬의 스택 충전 쿨타임이 끝났는지 확인한다.
     *
     * @return 스택 충전 쿨타임 종료 여부
     */
    public final boolean isStackCooldownFinished() {
        return getStackCooldown() == 0;
    }
}
