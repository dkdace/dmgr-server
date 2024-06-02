package com.dace.dmgr.combat.action.skill.module;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Confirmable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.MessageFormat;

/**
 * 스킬의 확인 모듈 클래스.
 *
 * <p>스킬이 {@link Confirmable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Confirmable
 */
@RequiredArgsConstructor
public class ConfirmModule {
    /** 스킬 객체 */
    @NonNull
    protected final Confirmable skill;
    /** 수락 키 */
    @NonNull
    protected final ActionKey acceptKey;
    /** 취소 키 */
    @NonNull
    protected final ActionKey cancelKey;

    /** 확인 중 상태 */
    @Getter
    protected boolean isChecking = false;

    /**
     * 스킬 확인 모드를 활성화 또는 비활성화한다.
     */
    public final void toggleCheck() {
        isChecking = !isChecking;

        if (isChecking) {
            skill.onCheckEnable();
            onCheckEnable();

            TaskUtil.addTask(skill, new IntervalTask(i -> {
                if (!isChecking)
                    return false;

                skill.onCheckTick(i);
                ConfirmModule.this.onCheckTick(i);

                return true;
            }, isCancelled -> {
                isChecking = false;
                onCheckDisable();
                skill.onCheckDisable();
            }, 1));
        }
    }

    /**
     * 모듈에서 확인 모드 활성화 시 실행할 작업.
     */
    protected void onCheckEnable() {
        // 미사용
    }

    /**
     * 모듈에서 확인 중에 매 틱마다 실행할 작업.
     *
     * @param i 인덱스
     */
    protected void onCheckTick(long i) {
        skill.getCombatUser().getUser().sendTitle("", MessageFormat.format("§7§l[{0}] §f사용     §7§l[{1}] §f취소",
                acceptKey.getName(), cancelKey.getName()), 0, 5, 5);
    }

    /**
     * 모듈에서 확인 모드 비활성화 시 실행할 작업.
     */
    protected void onCheckDisable() {
        // 미사용
    }
}
