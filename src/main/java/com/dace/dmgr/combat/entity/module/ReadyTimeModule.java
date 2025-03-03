package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.HasReadyTime;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 준비 대기시간이 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link HasReadyTime}를 상속받는 클래스여야 한다.
 *
 * @see HasReadyTime
 */
@RequiredArgsConstructor
@Getter
public final class ReadyTimeModule {
    /** 엔티티 객체 */
    @NonNull
    private final HasReadyTime combatEntity;
    /** 준비 시간 */
    private final long readyTime;
    /** 준비 완료 여부 */
    private boolean isReady = false;

    /**
     * 엔티티 준비 작업을 수행한다.
     */
    public void ready() {
        combatEntity.getTaskManager().add(new IntervalTask(i -> {
            if (i < readyTime)
                combatEntity.onTickBeforeReady(i);
            else if (i == readyTime) {
                isReady = true;
                combatEntity.onReady();
            }
        }, 1));
    }
}
