package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 여러 번 사용할 수 있는 스택형 스킬의 상태를 관리하는 클래스.
 */
public abstract class StackableSkill extends ActiveSkill {
    /** 기본 스택 충전 쿨타임 */
    @Getter
    protected final Timespan defaultStackCooldown;
    /** 최대 스택 충전량 */
    private final int maxStack;

    /** 스택 충전 쿨타임 타임스탬프 */
    private Timestamp stackCooldownTimestamp = Timestamp.now();
    /** 스킬 스택 수 */
    @Getter
    private int stack = 0;

    /**
     * 스택형 액티브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser           사용자 플레이어
     * @param activeSkillInfo      액티브 스킬 정보 인스턴스
     * @param defaultCooldown      기본 쿨타임
     * @param defaultStackCooldown 기본 스택 충전 쿨타임
     * @param defaultDuration      기본 지속시간
     * @param maxStack             최대 스택 충전량. 1 이상의 값
     * @param slot                 슬롯 번호. 0~4 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected StackableSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<?> activeSkillInfo, @NonNull Timespan defaultCooldown,
                             @NonNull Timespan defaultStackCooldown, @NonNull Timespan defaultDuration, int maxStack, int slot) {
        super(combatUser, activeSkillInfo, defaultCooldown, defaultDuration, slot);
        Validate.isTrue(maxStack >= 1, "maxStack >= 1 (%d)", maxStack);

        this.defaultStackCooldown = defaultStackCooldown;
        this.maxStack = maxStack;
        setStackCooldown(defaultStackCooldown);

        addOnReset(() -> {
            setStackCooldown(Timespan.ZERO);
            addStack(maxStack);
        });
    }

    @Override
    final void onTick() {
        if (stack > 0) {
            if (isDurationFinished())
                displayReady(stack);
            else
                displayUsing(stack);
        } else
            displayCooldown(1);
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && stack > 0;
    }

    /**
     * 스킬의 남은 스택 충전 쿨타임을 반환한다.
     *
     * @return 남은 스택 충전 쿨타임
     */
    @NonNull
    public final Timespan getStackCooldown() {
        return Timestamp.now().until(stackCooldownTimestamp);
    }

    /**
     * 스킬의 스택 충전 쿨타임을 설정한다.
     *
     * @param cooldown 스택 충전 쿨타임
     */
    public final void setStackCooldown(@NonNull Timespan cooldown) {
        if (stack >= maxStack)
            return;

        if (isStackCooldownFinished()) {
            stackCooldownTimestamp = Timestamp.now().plus(cooldown);

            if (!cooldown.isZero())
                runStackCooldown(cooldown);
        } else
            stackCooldownTimestamp = Timestamp.now().plus(cooldown);
    }

    /**
     * 스킬의 스택 충전 쿨타임을 실행한다.
     *
     * @param cooldown 스택 충전 쿨타임
     */
    private void runStackCooldown(@NonNull Timespan cooldown) {
        addTask(new IntervalTask(i -> {
            if (isStackCooldownFinished()) {
                onStackCooldownFinished();

                addStack(1);
                if (stack < maxStack)
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
     * 지정한 양만큼 스킬의 스택 수를 증가시킨다.
     *
     * @param amount 스택 증가량
     */
    public final void addStack(int amount) {
        stack = Math.min(Math.max(0, stack + amount), maxStack);

        if (stack > 0 && isStackCooldownFinished())
            setStackCooldown(defaultStackCooldown);
    }

    /**
     * 스킬의 스택 충전 쿨타임이 끝났는지 확인한다.
     *
     * @return 스택 충전 쿨타임 종료 여부
     */
    public final boolean isStackCooldownFinished() {
        return stackCooldownTimestamp.isBefore(Timestamp.now());
    }
}
