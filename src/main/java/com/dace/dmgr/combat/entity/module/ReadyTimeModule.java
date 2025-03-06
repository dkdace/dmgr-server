package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.HasReadyTime;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;

/**
 * 준비 대기시간이 있는 엔티티의 모듈 클래스.
 *
 * @see HasReadyTime
 */
@Getter
public final class ReadyTimeModule {
    /** 준비 완료 여부 */
    private boolean isReady = false;

    /**
     * 준비 대기시간 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity  대상 엔티티
     * @param readyDuration 준비 시간
     */
    public ReadyTimeModule(@NonNull HasReadyTime combatEntity, @NonNull Timespan readyDuration) {
        combatEntity.addTask(new IntervalTask(combatEntity::onTickBeforeReady, () -> {
            isReady = true;
            combatEntity.onReady();
        }, 1, readyDuration.toTicks()));
    }
}
