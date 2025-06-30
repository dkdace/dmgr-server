package com.dace.dmgr.combat.ability.skill.module;

import com.dace.dmgr.combat.ability.skill.Confirmable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * 스킬의 확인 모듈 클래스.
 *
 * @see Confirmable
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ConfirmModule {
    /** 스킬 인스턴스 */
    @NonNull
    protected final Confirmable skill;

    /** 확인 중 상태 */
    @Getter
    protected boolean isChecking = false;
    /** 틱 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask onTickTask;

    /**
     * 스킬 확인 모드를 활성화 또는 비활성화한다.
     */
    public final void toggleCheck() {
        if (isChecking) {
            cancel();
            return;
        }

        isChecking = true;
        onCheckEnable();

        onTickTask = new IntervalTask(this::onCheckTick, 1);
        skill.addTask(onTickTask);
    }

    /**
     * 스킬의 확인 모드를 수락한다.
     */
    public abstract void accept();

    /**
     * 스킬의 확인 모드를 취소한다.
     */
    public final void cancel() {
        if (!isChecking)
            return;

        isChecking = false;

        if (onTickTask != null)
            onTickTask.stop();

        onCheckDisable();
    }

    /**
     * 확인 모드 활성화 시 실행할 작업.
     */
    protected abstract void onCheckEnable();

    /**
     * 확인 중에 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    protected abstract void onCheckTick(long i);

    /**
     * 확인 모드 비활성화 시 실행할 작업.
     */
    protected abstract void onCheckDisable();
}
