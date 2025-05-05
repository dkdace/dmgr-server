package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.AbstractAction;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.info.SkillInfo;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

/**
 * {@link Skill}의 기본 구현체, 모든 스킬(패시브 스킬, 액티브 스킬)의 기반 클래스.
 */
public abstract class AbstractSkill extends AbstractAction implements Skill {
    /** 스킬 정보 인스턴스 */
    @NonNull
    @Getter
    protected final SkillInfo<?> skillInfo;
    /** 기본 지속시간 */
    @NonNull
    @Getter
    protected final Timespan defaultDuration;

    /** 틱 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask onTickTask;
    /** 지속시간 타임스탬프 */
    private Timestamp durationTimestamp = Timestamp.now();

    /**
     * 스킬 인스턴스를 생성한다.
     *
     * @param combatUser      사용자 플레이어
     * @param skillInfo       스킬 정보 인스턴스
     * @param defaultCooldown 기본 쿨타임
     * @param defaultDuration 기본 지속시간
     */
    protected AbstractSkill(@NonNull CombatUser combatUser, @NonNull SkillInfo<?> skillInfo, @NonNull Timespan defaultCooldown,
                            @NonNull Timespan defaultDuration) {
        super(combatUser, defaultCooldown);

        this.skillInfo = skillInfo;
        this.defaultDuration = defaultDuration;
        setCooldown(defaultCooldown);

        addOnReset(() -> setDuration(Timespan.ZERO));
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownSet() {
        if (!isDurationFinished())
            setDuration(Timespan.ZERO);
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.USE_SKILL);
    }

    @Override
    @NonNull
    public final Timespan getDuration() {
        return Timestamp.now().until(durationTimestamp);
    }

    @Override
    public final void setDuration(@NonNull Timespan duration) {
        if (isDurationFinished()) {
            durationTimestamp = Timestamp.now().plus(duration);

            if (!duration.isZero())
                runDuration();

            return;
        }

        durationTimestamp = Timestamp.now().plus(duration);
        if (duration.isZero())
            stopDuration();
    }

    @Override
    public final void setDuration() {
        setDuration(defaultDuration);
    }

    @Override
    public final void addDuration(@NonNull Timespan duration) {
        setDuration(getDuration().plus(duration));
    }

    /**
     * 스킬의 지속시간 태스크를 실행한다.
     */
    private void runDuration() {
        if (onTickTask != null)
            return;

        onTickTask = new IntervalTask(i -> {
            if (isDurationFinished()) {
                stopDuration();
                return false;
            }

            return true;
        }, 1);

        addTask(onTickTask);
    }

    /**
     * 스킬의 지속시간 태스크를 종료한다.
     */
    private void stopDuration() {
        if (onTickTask == null)
            return;

        onTickTask.stop();
        onTickTask = null;
        onDurationFinished();
    }

    /**
     * 지속시간이 끝났을 때 실행할 작업.
     */
    @MustBeInvokedByOverriders
    protected void onDurationFinished() {
        if (isCooldownFinished())
            setCooldown();
    }

    @Override
    public final boolean isDurationFinished() {
        return durationTimestamp.isBefore(Timestamp.now());
    }
}
