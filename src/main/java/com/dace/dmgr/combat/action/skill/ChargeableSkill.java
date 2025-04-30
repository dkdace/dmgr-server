package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 상태 변수를 가지고 있는 충전형 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class ChargeableSkill extends ActiveSkill {
    /** 상태 변수의 최댓값 */
    protected final double maxStateValue;
    /** 상태 변수 */
    private double stateValue = 0;

    /**
     * 충전형 액티브 스킬 인스턴스를 생성한다.
     *
     * @param combatUser      사용자 플레이어
     * @param activeSkillInfo 액티브 스킬 정보 인스턴스
     * @param defaultCooldown 기본 쿨타임
     * @param maxStateValue   상태 변수의 최댓값. 0을 초과하는 값
     * @param slot            슬롯 번호. 0~4 사이의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    protected ChargeableSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<?> activeSkillInfo, @NonNull Timespan defaultCooldown,
                              double maxStateValue, int slot) {
        super(combatUser, activeSkillInfo, defaultCooldown, Timespan.MAX, slot);

        Validate.isTrue(maxStateValue > 0, "maxStateValue > 0 (%f)", maxStateValue);
        this.maxStateValue = maxStateValue;

        addOnReset(() -> setStateValue(maxStateValue));
    }

    @Override
    final void onTick() {
        if (isDurationFinished()) {
            if (isCooldownFinished())
                displayReady(1);
            else
                displayCooldown((int) Math.ceil(getCooldown().toSeconds()));
        } else {
            displayUsing(1);
            addStateValue(-getStateValueDecrement() / 20.0);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        runStateValueCharge();
    }

    /**
     * 스킬의 상태 변수 충전을 실행한다.
     */
    private void runStateValueCharge() {
        addTask(new IntervalTask(i -> {
            addStateValue(getStateValueIncrement() / 20.0);

            return (stateValue < maxStateValue) && isDurationFinished() && isCooldownFinished();
        }, 1));
    }

    /**
     * 스킬의 상태 변수를 설정한다.
     *
     * @param stateValue 상태 변수
     */
    public final void setStateValue(double stateValue) {
        this.stateValue = Math.min(Math.max(0, stateValue), maxStateValue);
    }

    /**
     * 지정한 양만큼 스킬의 상태 변수를 증가시킨다.
     *
     * @param increment 증가량
     */
    public final void addStateValue(double increment) {
        setStateValue(stateValue + increment);
    }

    /**
     * 상태 변수의 초당 충전량을 반환한다.
     *
     * @return 상태 변수의 초당 충전량
     */
    protected abstract double getStateValueIncrement();

    /**
     * 상태 변수의 초당 소모량을 반환한다.
     *
     * @return 상태 변수의 초당 소모량
     */
    protected abstract double getStateValueDecrement();
}
