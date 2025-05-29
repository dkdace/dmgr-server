package com.dace.dmgr.combat.entity.combatuser;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;

import java.util.function.LongConsumer;

/**
 * 화면 흔들림 효과를 나타내는 클래스.
 */
@AllArgsConstructor
public final class ScreenShake {
    /** Yaw 분산도 */
    private final double yawSpread;
    /** Pitch 분산도 */
    private final double pitchSpread;
    /** 지속 시간 */
    private final Timespan duration;

    /**
     * 지정한 플레이어에게 화면 흔들림 효과를 전송한다.
     */
    public void send(@NonNull CombatUser combatUser) {
        combatUser.addTask(new IntervalTask((LongConsumer) i ->
                combatUser.addYawAndPitch(
                        RandomUtils.nextDouble(0, yawSpread) - RandomUtils.nextDouble(0, yawSpread),
                        RandomUtils.nextDouble(0, pitchSpread) - RandomUtils.nextDouble(0, pitchSpread)), 1, duration.toTicks()));
    }
}
