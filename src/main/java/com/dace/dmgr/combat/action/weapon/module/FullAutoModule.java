package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
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
    /** 연사 기능을 적용할 동작 사용 키 */
    @NonNull
    @Getter
    private final ActionKey fullAutoKey;
    /** 연사속도 */
    @NonNull
    private final FullAuto.FireRate fireRate;

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
        return (fireRate.getTickFlag() & 1 << tick % 20) != 0;
    }

    /**
     * 무기의 연사 기능을 사용한다.
     */
    public final void use() {
        Timestamp expiration = Timestamp.now().plus(Timespan.ofTicks(6));

        if (cooldownTimestamp.isAfter(Timestamp.now())) {
            cooldownTimestamp = expiration;

            if (fullAutoTask != null && !fullAutoTask.isDisposed())
                return;
        }

        cooldownTimestamp = expiration;

        fullAutoTask = new IntervalTask(i -> {
            if (cooldownTimestamp.isBefore(Timestamp.now()))
                return false;

            if (weapon.canUse(fullAutoKey) && !weapon.getCombatUser().isDead() && weapon.getCombatUser().isGlobalCooldownFinished() && isFireTick(i))
                weapon.onUse(fullAutoKey);

            return true;
        }, 1);

        weapon.addActionTask(fullAutoTask);
    }
}
