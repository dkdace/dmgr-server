package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.Slow;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.Getter;
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
        victim.getStatusEffectModule().applyStatusEffect(StatusEffectType.SLOW, FreezeValue.instance, JagerT1Info.DURATION);
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
        @NonNull
        public String getName() {
            return super.getName() + MODIFIER_ID;
        }

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, long i) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.CONCRETE, 3, combatEntity.getEntity().getLocation().add(0, 0.5, 0),
                    1, 0.25, 0, 0.25, 0);

            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID,
                        -combatEntity.getPropertyManager().getValue(Property.FREEZE));
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity) {
            combatEntity.getPropertyManager().setValue(Property.FREEZE, 0);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    /**
     * 빙결 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Freeze extends Snare {
        @Getter
        private static final Freeze instance = new Freeze();

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, long i) {
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l얼어붙음!", "", 0, 2, 10);

            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE,
                    combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() / 2, 0), 5,
                    0.4F, 0.8F, 0.4F, 120, 220, 240);
        }
    }
}
