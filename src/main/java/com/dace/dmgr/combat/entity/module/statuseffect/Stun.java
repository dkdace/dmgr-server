package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 기절 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stun implements StatusEffect {
    @Getter
    static final Stun instance = new Stun();

    @Override
    @NonNull
    public String getName() {
        return "기절";
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).cancelAction();
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l기절함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        // 미사용
    }
}
