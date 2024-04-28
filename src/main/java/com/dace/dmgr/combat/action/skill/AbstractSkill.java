package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.Cooldown;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * {@link Skill}의 기본 구현체, 모든 스킬(패시브 스킬, 액티브 스킬)의 기반 클래스.
 */
@Getter
public abstract class AbstractSkill extends AbstractAction implements Skill {
    /** 스킬 정보 객체 */
    @NonNull
    protected final SkillInfo skillInfo;
    /** 스킬 아이템 객체 */
    @NonNull
    protected ItemStack itemStack;

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param skillInfo  스킬 정보 객체
     */
    protected AbstractSkill(@NonNull CombatUser combatUser, @NonNull SkillInfo skillInfo) {
        super(combatUser);

        this.skillInfo = skillInfo;
        this.itemStack = skillInfo.getItemStack();
        setCooldown(getDefaultCooldown());
    }

    @Override
    protected void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(0);
    }

    @Override
    public final long getDuration() {
        return CooldownUtil.getCooldown(this, Cooldown.SKILL_DURATION);
    }

    @Override
    public final void setDuration(long duration) {
        if (isDurationFinished()) {
            CooldownUtil.setCooldown(this, Cooldown.SKILL_DURATION, duration);
            runDuration();
        } else {
            CooldownUtil.setCooldown(this, Cooldown.SKILL_DURATION, duration);
            if (duration == 0)
                onDurationFinished();
        }
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
        TaskUtil.addTask(this, new IntervalTask(i -> {
            if (isDurationFinished()) {
                onDurationFinished();
                return false;
            }

            return true;
        }, 1));
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     */
    protected void onDurationFinished() {
        if (isCooldownFinished())
            setCooldown();
    }

    @Override
    public final boolean isDurationFinished() {
        return getDuration() == 0;
    }
}
