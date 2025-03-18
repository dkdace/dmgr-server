package com.dace.dmgr.combat.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.Task;
import com.dace.dmgr.util.task.TaskManager;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;

/**
 * {@link Action}의 기본 구현체, 모든 동작(무기, 스킬)의 기반 클래스.
 *
 * @see AbstractWeapon
 * @see AbstractSkill
 */
public abstract class AbstractAction implements Action {
    /** 사용자 플레이어 */
    @NonNull
    @Getter
    protected final CombatUser combatUser;
    /** 기본 쿨타임 */
    @NonNull
    @Getter
    protected final Timespan defaultCooldown;
    /** 태스크 관리 인스턴스 */
    private final TaskManager taskManager = new TaskManager();
    /** 동작 태스크 관리 인스턴스 */
    private final TaskManager actionTaskManager = new TaskManager();
    /** 초기화 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onResets = new ArrayList<>();
    /** 제거 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onRemoves = new ArrayList<>();

    /** 쿨타임 타임스탬프 */
    private Timestamp cooldownTimestamp = Timestamp.now();
    /** 제거 여부 */
    private boolean isRemoved = false;

    /**
     * 동작 인스턴스를 생성한다.
     *
     * @param combatUser      사용자 플레이어
     * @param defaultCooldown 기본 쿨타임
     */
    protected AbstractAction(@NonNull CombatUser combatUser, @NonNull Timespan defaultCooldown) {
        this.combatUser = combatUser;
        this.defaultCooldown = defaultCooldown;
    }

    @Override
    public final void addActionTask(@NonNull Task task) {
        if (!isRemoved)
            actionTaskManager.add(task);
    }

    @Override
    public final void addTask(@NonNull Task task) {
        if (!isRemoved)
            taskManager.add(task);
    }

    @Override
    public final void addOnReset(@NonNull Runnable onReset) {
        if (!isRemoved)
            onResets.add(onReset);
    }

    @Override
    public final void addOnRemove(@NonNull Runnable onRemove) {
        if (!isRemoved)
            onRemoves.add(onRemove);
    }

    @Override
    @NonNull
    public final Timespan getCooldown() {
        return Timestamp.now().until(cooldownTimestamp);
    }

    @Override
    public final void setCooldown(@NonNull Timespan cooldown) {
        if (isCooldownFinished()) {
            cooldownTimestamp = Timestamp.now().plus(cooldown);
            onCooldownSet();

            if (!cooldown.isZero())
                runCooldown();
        } else
            cooldownTimestamp = Timestamp.now().plus(cooldown);
    }

    @Override
    public final void setCooldown() {
        setCooldown(defaultCooldown);
    }

    @Override
    public final void addCooldown(@NonNull Timespan cooldown) {
        setCooldown(getCooldown().plus(cooldown));
    }

    /**
     * 동작의 쿨타임을 실행한다.
     */
    private void runCooldown() {
        addTask(new IntervalTask(i -> {
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
        return cooldownTimestamp.isBefore(Timestamp.now());
    }

    @Override
    @MustBeInvokedByOverriders
    public boolean canUse(@NonNull ActionKey actionKey) {
        return isCooldownFinished() && !combatUser.getStatusEffectModule().hasRestriction(CombatRestriction.USE_ACTION);
    }

    @Override
    public final boolean cancel() {
        if (!isCancellable() && !combatUser.isDead())
            return false;

        actionTaskManager.stop();
        onCancelled();

        return true;
    }

    /**
     * 취소 가능 조건 ({@link Action#isCancellable()})을 무시하고 동작 사용을 강제로 취소시킨다.
     */
    protected final void forceCancel() {
        actionTaskManager.stop();
        onCancelled();
    }

    /**
     * 동작 사용이 취소되었을 때 실행할 작업.
     */
    protected void onCancelled() {
        // 미사용
    }

    @Override
    public final void reset() {
        setCooldown(defaultCooldown);

        for (Runnable onReset : onResets) {
            onReset.run();

            if (isRemoved) {
                onResets.clear();
                break;
            }
        }
    }

    @Override
    public final void remove() {
        Validate.validState(!isRemoved, "Action이 이미 제거됨");

        reset();
        onRemoves.forEach(Runnable::run);
        onRemoves.clear();

        actionTaskManager.stop();
        taskManager.stop();

        isRemoved = true;
    }
}
