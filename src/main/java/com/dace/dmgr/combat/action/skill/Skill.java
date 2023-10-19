package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.Action;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;

/**
 * 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class Skill extends Action {
    /** 번호 */
    protected final int number;

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param number     번호
     * @param combatUser 대상 플레이어
     * @param skillInfo  스킬 정보 객체
     */
    protected Skill(int number, CombatUser combatUser, SkillInfo skillInfo) {
        super(combatUser, skillInfo);
        this.number = number;
        setCooldown(getDefaultCooldown());
    }

    @Override
    protected void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(0);
    }

    /**
     * 스킬의 기본 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    public abstract long getDefaultDuration();

    /**
     * 스킬의 남은 지속시간을 반환한다.
     *
     * @return 지속시간 (tick)
     */
    public final long getDuration() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION);
    }

    /**
     * 스킬의 지속시간을 설정한다.
     *
     * @param duration 지속시간 (tick). {@code -1}로 설정 시 무한 지속
     */
    public final void setDuration(long duration) {
        if (isDurationFinished()) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
            runDuration();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    /**
     * 스킬의 지속시간을 기본 지속시간으로 설정한다.
     *
     * @see Skill#getDefaultDuration()
     */
    public final void setDuration() {
        setDuration(getDefaultDuration());
    }

    /**
     * 스킬의 지속시간을 증가시킨다.
     *
     * @param duration 추가할 지속시간 (tick)
     */
    public final void addDuration(long duration) {
        setDuration(getDuration() + duration);
    }

    /**
     * 스킬의 지속시간 스케쥴러를 실행한다.
     */
    protected final void runDuration() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                onDurationTick();

                if (isDurationFinished()) {
                    onDurationFinished();
                    return false;
                }

                return true;
            }
        };
    }

    /**
     * 지속시간이 진행할 때 (매 tick마다) 실행할 작업.
     */
    protected void onDurationTick() {
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     */
    protected void onDurationFinished() {
        if (isCooldownFinished())
            setCooldown();
    }

    /**
     * 스킬의 지속시간이 끝났는 지 확인한다.
     *
     * @return 지속시간 종료 여부
     */
    public final boolean isDurationFinished() {
        return getDuration() == 0;
    }
}
