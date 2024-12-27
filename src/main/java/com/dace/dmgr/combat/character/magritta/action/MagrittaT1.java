package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.effect.TextHologram;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

@UtilityClass
public final class MagrittaT1 {
    /**
     * 피격자의 파쇄 수치를 증가시킨다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     */
    static void addShreddingValue(@NonNull CombatUser attacker, @NonNull Damageable victim) {
        if (victim.getPropertyManager().getValue(Property.SHREDDING) == 0)
            victim.getStatusEffectModule().applyStatusEffect(attacker, new ShreddingValue(), MagrittaT1Info.DURATION);

        victim.getPropertyManager().addValue(Property.SHREDDING, 1);
        if (victim.getPropertyManager().getValue(Property.SHREDDING) >= MagrittaT1Info.MAX) {
            victim.getStatusEffectModule().applyStatusEffect(attacker, MagrittaT1Burning.instance, MagrittaT1Info.DURATION);

            MagrittaT1Info.SOUND.MAX.play(victim.getEntity().getLocation());

            if (victim instanceof CombatUser)
                attacker.addScore("파쇄", MagrittaT1Info.MAX_DAMAGE_SCORE);
        }

        MagrittaT1Info.SOUND.USE.play(victim.getEntity().getLocation());
    }

    /**
     * 파쇄 수치 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ShreddingValue implements StatusEffect {
        /** 파쇄 수치 */
        private int shredding = 0;
        /** 파쇄 수치 홀로그램 */
        @Nullable
        private TextHologram shreddingHologram;

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (!combatEntity.getDamageModule().isLiving() || !(provider instanceof CombatUser))
                return;

            shreddingHologram = new TextHologram(combatEntity.getEntity(), target -> {
                if (target == provider.getEntity())
                    return LocationUtil.canPass(target.getEyeLocation(), combatEntity.getCenterLocation());

                return false;
            }, 1);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (!combatEntity.getDamageModule().isLiving() || !(provider instanceof CombatUser) || shreddingHologram == null)
                return;

            int currentShredding = combatEntity.getPropertyManager().getValue(Property.SHREDDING);
            if (shredding < currentShredding) {
                shredding = currentShredding;

                combatEntity.getStatusEffectModule().applyStatusEffect(provider, this, MagrittaT1Info.DURATION);
                shreddingHologram.setContent(MessageFormat.format("§c{0} §f{1}", TextIcon.DAMAGE_INCREASE, currentShredding));
            }
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getPropertyManager().setValue(Property.SHREDDING, 0);

            if (combatEntity.getDamageModule().isLiving() && shreddingHologram != null)
                shreddingHologram.dispose();
        }
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class MagrittaT1Burning extends Burning {
        private static final MagrittaT1Burning instance = new MagrittaT1Burning();

        private MagrittaT1Burning() {
            super(MagrittaT1Info.FIRE_DAMAGE_PER_SECOND, true);
        }
    }
}
