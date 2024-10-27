package com.dace.dmgr.combat.action;

import com.dace.dmgr.Disposable;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * {@link Action}의 기본 구현체, 모든 동작(무기, 스킬)의 기반 클래스.
 *
 * @see AbstractWeapon
 * @see AbstractSkill
 */
@Getter
public abstract class AbstractAction implements Action {
    /** 동작 쿨타임 ID */
    protected static final String ACTION_COOLDOWN_ID = "ActionCooldown";

    /** 플레이어 객체 */
    @NonNull
    protected final CombatUser combatUser;
    /** 비활성화 여부 */
    private boolean isDisposed = false;
    /** 동작 태스크 실행 객체 */
    @NonNull
    protected final Disposable taskRunner = new Disposable() {
        @Override
        public void dispose() {
            throw new UnsupportedOperationException("TaskRunner는 폐기될 수 없음");
        }

        @Override
        public boolean isDisposed() {
            return AbstractAction.this.isDisposed();
        }
    };

    /**
     * 동작 인스턴스를 생성한다.
     *
     * @param combatUser 대상 플레이어
     */
    protected AbstractAction(@NonNull CombatUser combatUser) {
        this.combatUser = combatUser;
    }

    @Override
    public final long getCooldown() {
        return CooldownUtil.getCooldown(this, ACTION_COOLDOWN_ID);
    }

    @Override
    public final void setCooldown(long cooldown) {
        if (cooldown < -1)
            throw new IllegalArgumentException("'cooldown'이 -1 이상이어야 함");

        if (isCooldownFinished()) {
            CooldownUtil.setCooldown(this, ACTION_COOLDOWN_ID, cooldown);
            runCooldown();
            onCooldownSet();
        } else
            CooldownUtil.setCooldown(this, ACTION_COOLDOWN_ID, cooldown);
    }

    @Override
    public final void setCooldown() {
        setCooldown(getDefaultCooldown());
    }

    @Override
    public final void addCooldown(long cooldown) {
        if (cooldown < 0)
            throw new IllegalArgumentException("'cooldown'이 0 이상이어야 함");

        setCooldown(getCooldown() + cooldown);
    }

    /**
     * 쿨타임 스케쥴러를 실행한다.
     */
    private void runCooldown() {
        TaskUtil.addTask(this, new IntervalTask(i -> {
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return isCooldownFinished()
                && !combatUser.getStatusEffectModule().hasAllRestrictions(CombatRestrictions.USE_ACTION);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onCancelled() {
        TaskUtil.clearTask(taskRunner);
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        validate();
        setCooldown(getDefaultCooldown());
    }

    @Override
    @MustBeInvokedByOverriders
    public void dispose() {
        validate();

        reset();
        TaskUtil.clearTask(taskRunner);
        TaskUtil.clearTask(this);
        isDisposed = true;
    }
}
