package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

/**
 * {@link Skill}의 기본 구현체, 모든 스킬(패시브 스킬, 액티브 스킬)의 기반 클래스.
 */
public abstract class AbstractSkill extends AbstractAction implements Skill {
    /** 스킬 지속시간 쿨타임 ID */
    protected static final String SKILL_DURATION_COOLDOWN_ID = "SkillDuration";

    /** 원본 스킬 아이템 객체 */
    protected final ItemStack originalItemStack;
    /** 스킬 아이템 객체 */
    protected ItemStack itemStack;

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     * @param skillInfo  스킬 정보 객체
     */
    protected AbstractSkill(@NonNull CombatUser combatUser, @NonNull SkillInfo<? extends Skill> skillInfo) {
        super(combatUser);

        this.originalItemStack = skillInfo.getStaticItem().getItemStack();
        this.itemStack = originalItemStack.clone();
        setCooldown(getDefaultCooldown());
    }

    @Override
    protected void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(0);
    }

    @Override
    public final long getDuration() {
        return CooldownUtil.getCooldown(this, SKILL_DURATION_COOLDOWN_ID);
    }

    @Override
    public final void setDuration(long duration) {
        if (duration < -1)
            throw new IllegalArgumentException("'duration'이 -1 이상이어야 함");

        if (isDurationFinished()) {
            CooldownUtil.setCooldown(this, SKILL_DURATION_COOLDOWN_ID, duration);
            runDuration();
        } else {
            CooldownUtil.setCooldown(this, SKILL_DURATION_COOLDOWN_ID, duration);
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
        if (duration < 0)
            throw new IllegalArgumentException("'duration'이 0 이상이어야 함");

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
