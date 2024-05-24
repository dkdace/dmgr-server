package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 속도 증가 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Speed implements StatusEffect {
    @Getter
    static final Speed instance = new Speed();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.SPEED;
    }

    @Override
    public final boolean isPositive() {
        return true;
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB_AMBIENT, combatEntity.getEntity().getLocation().add(0, 0.1, 0),
                3, 0.3, 0, 0.3, 200, 255, 255);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        // 미사용
    }
}
