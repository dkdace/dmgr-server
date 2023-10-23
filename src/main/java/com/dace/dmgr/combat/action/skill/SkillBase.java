package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.ActionBase;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.Cooldown;
import com.dace.dmgr.system.CooldownManager;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;

/**
 * 모든 스킬의 기반 클래스.
 *
 * @see ActiveSkill
 * @see UltimateSkill
 */
@Getter
public abstract class SkillBase extends ActionBase implements Skill {
    /** 스킬 번호 */
    private final int number;

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param number     번호
     * @param combatUser 대상 플레이어
     * @param skillInfo  스킬 정보 객체
     */
    protected SkillBase(int number, CombatUser combatUser, SkillInfo skillInfo) {
        super(combatUser, skillInfo);
        this.number = number;
        setCooldown(getDefaultCooldown());
    }

    @Override
    public void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(0);
    }

    @Override
    public void onCooldownTick() {
    }

    @Override
    public void onCooldownFinished() {
    }

    @Override
    public final long getDuration() {
        return CooldownManager.getCooldown(this, Cooldown.SKILL_DURATION);
    }

    @Override
    public final void setDuration(long duration) {
        if (isDurationFinished()) {
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
            runDuration();
        } else
            CooldownManager.setCooldown(this, Cooldown.SKILL_DURATION, duration);
    }

    @Override
    public final void setDuration() {
        setDuration(getDefaultDuration());
    }

    @Override
    public final void addDuration(long duration) {
        setDuration(getDuration() + duration);
    }

    /**
     * 스킬의 지속시간 스케쥴러를 실행한다.
     */
    private void runDuration() {
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
    @Override
    public void onDurationTick() {
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     */
    @Override
    public void onDurationFinished() {
        if (isCooldownFinished())
            setCooldown();
    }

    @Override
    public final boolean isDurationFinished() {
        return getDuration() == 0;
    }
}
