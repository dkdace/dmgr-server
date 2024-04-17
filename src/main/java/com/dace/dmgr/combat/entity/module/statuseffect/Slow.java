package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;

/**
 * 둔화 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Slow implements StatusEffect {
    @Getter
    static final Slow instance = new Slow();

    @Override
    @NonNull
    public String getName() {
        return "둔화";
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.WOOL, 12, combatEntity.getEntity().getLocation().add(0, 0.5, 0),
                3, 0.3, 0, 0.3, 0);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        // 미사용
    }
}
