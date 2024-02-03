package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 속박 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Snare implements StatusEffect {
    @Getter
    private static final Snare instance = new Snare();

    @Override
    @NonNull
    public String getName() {
        return "속박";
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l속박당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
    }
}
