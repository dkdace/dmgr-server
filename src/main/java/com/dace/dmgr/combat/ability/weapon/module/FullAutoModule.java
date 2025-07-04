package com.dace.dmgr.combat.ability.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

/**
 * 무기의 연사 모듈 클래스.
 *
 * @see FullAuto
 */
@RequiredArgsConstructor
public class FullAutoModule {
    /** 무기 인스턴스 */
    @NonNull
    protected final FullAuto weapon;

    /** 연사 무기 사용을 처리하는 태스크 */
    @Nullable
    private IntervalTask fullAutoTask;
    /** 쿨타임 타임스탬프 */
    private Timestamp cooldownTimestamp = Timestamp.now();

    /**
     * 틱을 기준으로 발사할 수 있는 시점을 확인한다.
     *
     * @param tick 기준 틱
     * @return 발사 가능 여부
     */
    private boolean isFireTick(long tick) {
        return (weapon.getFireRate().getTickFlag() & 1 << tick % 20) != 0;
    }

    /**
     * 무기를 사용했을 때 실행할 작업.
     */
    public void onUse() {
        Timestamp expiration = Timestamp.now().plus(Timespan.ofTicks(6));

        if (fullAutoTask != null && !fullAutoTask.isStopped()) {
            cooldownTimestamp = expiration;
            return;
        }

        cooldownTimestamp = expiration;

        fullAutoTask = new IntervalTask(i -> {
            if (cooldownTimestamp.isBefore(Timestamp.now()))
                return false;

            if (weapon.getCombatUser().isGlobalCooldownFinished() && isFireTick(i))
                weapon.use(ActionKey.RIGHT_CLICK);

            return true;
        }, 1);

        weapon.addActionTask(fullAutoTask);
    }
}
