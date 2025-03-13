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
    /** 초기화 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onResets = new ArrayList<>();
    /** 제거 시 실행할 작업 목록 */
    private final ArrayList<Runnable> onDisposes = new ArrayList<>();

    /** 동작 태스크 관리 인스턴스 */
    private TaskManager actionTaskManager = new TaskManager();
    /** 쿨타임 타임스탬프 */
    private Timestamp cooldownTimestamp = Timestamp.now();
    /** 비활성화 여부 */
    @Getter
    private boolean isDisposed = false;

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
        actionTaskManager.add(task);
    }

    @Override
    public final void addTask(@NonNull Task task) {
        taskManager.add(task);
    }

    @Override
    public final void addOnReset(@NonNull Runnable onReset) {
        onResets.add(onReset);
    }

    @Override
    public final void addOnDispose(@NonNull Runnable onDispose) {
        onDisposes.add(onDispose);
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
            runCooldown();
            onCooldownSet();
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
    @MustBeInvokedByOverriders
    public void onCancelled() {
        actionTaskManager.dispose();
        actionTaskManager = new TaskManager();
    }

    @Override
    public final void reset() {
        setCooldown(defaultCooldown);
        onResets.forEach(Runnable::run);
    }

    @Override
    public final void dispose() {
        if (isDisposed)
            throw new IllegalStateException("인스턴스가 이미 폐기됨");

        reset();
        onResets.clear();
        onDisposes.forEach(Runnable::run);
        onDisposes.clear();

        actionTaskManager.dispose();
        taskManager.dispose();

        isDisposed = true;
    }
}
