package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;

@UtilityClass
public final class JagerT1 {
    /**
     * 피격자의 빙결 수치를 증가시킨다.
     *
     * @param victim 피격자
     * @param amount 증가량
     */
    static void addFreezeValue(@NonNull CombatEntity victim, int amount) {
        victim.getPropertyManager().addValue(Property.FREEZE, amount);
        victim.getStatusEffectModule().applyStatusEffect(victim, FreezeValue.instance, JagerT1Info.DURATION);
    }

    /**
     * 빙결 수치 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FreezeValue extends Slow {
        private static final FreezeValue instance = new FreezeValue();
        /** 수정자 ID */
        private static final String MODIFIER_ID = "JagerT1";

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.CONCRETE, 3, combatEntity.getEntity().getLocation().add(0, 0.5, 0),
                    1, 0.25, 0, 0.25, 0);

            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID,
                        -combatEntity.getPropertyManager().getValue(Property.FREEZE));
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getPropertyManager().setValue(Property.FREEZE, 0);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }
}
