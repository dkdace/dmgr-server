package com.dace.dmgr.combat.entity.temporary.dummy;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.util.task.DelayTask;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RandomUtils;

/**
 * 총알을 발사하여 공격하는 더미의 행동 양식 클래스.
 */
@NoArgsConstructor
public final class ShootingBehavior implements DummyBehavior {
    @Override
    public void onInit(@NonNull Dummy dummy) {
        if (!dummy.getStatusEffectModule().hasRestriction(CombatRestriction.USE_WEAPON))
            new DummyProjectile(dummy, 100).shot();

        dummy.addTask(new DelayTask(() -> onInit(dummy), RandomUtils.nextInt(20, 30)));
    }
}
