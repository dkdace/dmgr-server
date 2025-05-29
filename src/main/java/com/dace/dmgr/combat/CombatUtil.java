package com.dace.dmgr.combat;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomUtils;

import java.util.function.LongConsumer;

/**
 * 전투 시스템에 사용되는 기능을 제공하는 클래스.
 */
@UtilityClass
public final class CombatUtil {
    /**
     * 지정한 플레이어에게 화면 흔들림 효과를 전송한다.
     *
     * @param combatUser  대상 플레이어
     * @param yawSpread   Yaw 분산도
     * @param pitchSpread Pitch 분산도
     * @param duration    진행 시간
     * @see CombatUtil#sendShake(CombatUser, double, double)
     */
    public static void sendShake(@NonNull CombatUser combatUser, double yawSpread, double pitchSpread, @NonNull Timespan duration) {
        combatUser.addTask(new IntervalTask((LongConsumer) i -> sendShake(combatUser, yawSpread, pitchSpread), 1, duration.toTicks()));
    }

    /**
     * 지정한 플레이어에게 화면 흔들림 효과를 전송한다.
     *
     * @param combatUser  대상 플레이어
     * @param yawSpread   Yaw 분산도
     * @param pitchSpread Pitch 분산도
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public static void sendShake(@NonNull CombatUser combatUser, double yawSpread, double pitchSpread) {
        combatUser.addYawAndPitch(
                RandomUtils.nextDouble(0, yawSpread) - RandomUtils.nextDouble(0, yawSpread),
                RandomUtils.nextDouble(0, pitchSpread) - RandomUtils.nextDouble(0, pitchSpread));
    }
}
