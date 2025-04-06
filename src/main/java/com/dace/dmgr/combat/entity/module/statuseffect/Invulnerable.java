package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 무적 상태 효과를 처리하는 클래스.
 */
public class Invulnerable extends StatusEffect {
    @Getter
    private static final Invulnerable instance = new Invulnerable();

    /**
     * 무적 상태 효과 인스턴스를 생성한다.
     */
    protected Invulnerable() {
        super(StatusEffectType.INVULNERABLE, true);
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, long i) {
        // 미사용
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.DAMAGED);
    }
}
