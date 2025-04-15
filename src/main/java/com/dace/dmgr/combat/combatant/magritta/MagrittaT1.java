package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.module.statuseffect.ValueStatusEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.util.LocationUtil;
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
        ShreddingValue shreddingValue = victim.getStatusEffectModule().apply(ValueStatusEffect.Type.SHREDDING, MagrittaT1Info.DURATION);
        shreddingValue.addValue(attacker, victim);

        MagrittaT1Info.SOUND.USE.play(victim.getLocation());
    }

    /**
     * 파쇄 수치 상태 효과 클래스.
     */
    public static final class ShreddingValue extends ValueStatusEffect {
        /** 파쇄 수치 홀로그램 */
        @Nullable
        private TextHologram shreddingHologram;
        /** 화염 상태 효과 */
        @Nullable
        private Burning burning;

        public ShreddingValue() {
            super(StatusEffectType.NONE, false, MagrittaT1Info.MAX);
        }

        private void addValue(@NonNull CombatUser attacker, @NonNull Damageable victim) {
            setValue(getValue() + 1);

            if (victim.isCreature()) {
                if (shreddingHologram == null)
                    shreddingHologram = new TextHologram(victim.getEntity(), target -> {
                        if (target == attacker.getEntity())
                            return LocationUtil.canPass(target.getEyeLocation(), victim.getCenterLocation());

                        return false;
                    }, 1);

                shreddingHologram.setContent(MessageFormat.format("§c{0} §f{1}", TextIcon.DAMAGE_INCREASE, getValue()));
            }

            if (getValue() >= MagrittaT1Info.MAX)
                onMaxValue(attacker, victim);
        }

        private void onMaxValue(@NonNull CombatUser attacker, @NonNull Damageable victim) {
            if (burning == null)
                burning = new Burning(attacker, MagrittaT1Info.FIRE_DAMAGE_PER_SECOND, true);

            victim.getStatusEffectModule().apply(burning, MagrittaT1Info.DURATION);

            MagrittaT1Info.SOUND.MAX.play(victim.getLocation());

            if (victim.isGoalTarget())
                attacker.addScore("파쇄", MagrittaT1Info.MAX_DAMAGE_SCORE);
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
            setValue(0);
            burning = null;

            if (shreddingHologram != null) {
                shreddingHologram.remove();
                shreddingHologram = null;
            }
        }
    }
}
