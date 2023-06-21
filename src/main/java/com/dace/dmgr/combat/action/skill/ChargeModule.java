package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;

import static com.dace.dmgr.system.HashMapList.combatUserMap;

/**
 * 스킬의 충전형 모듈 클래스.
 */
public class ChargeModule {
    /** 스킬 객체 */
    private final Skill skill;

    /** 상태 변수 */
    @Getter
    private float stateValue = 0;

    public ChargeModule(Skill skill) {
        this.skill = skill;
        this.stateValue = ((Chargeable) skill).getMaxStateValue();
    }

    /**
     * 스킬의 상태 변수 충전을 실행한다.
     */
    private void runStateValueCharge() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (combatUserMap.get(skill.getCombatUser().getEntity()) == null)
                    return false;

                addStateValue(((Chargeable) skill).getStateValueIncrement() / 20F);

                return stateValue < ((Chargeable) skill).getMaxStateValue() && !skill.isUsing() && skill.isCooldownFinished();
            }
        };
    }

    /**
     * 지정한 양만큼 스킬의 상태 변수를 증가시킨다.
     *
     * <p>스킬이 {@link Chargeable}을 상속받는 클래스여야 한다.</p>
     *
     * @param increment 증가량
     * @see Chargeable
     */
    public void addStateValue(float increment) {
        stateValue += increment;
        if (stateValue < 0)
            stateValue = 0;
        if (stateValue > ((Chargeable) skill).getMaxStateValue())
            stateValue = ((Chargeable) skill).getMaxStateValue();
    }
}
