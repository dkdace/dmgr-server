package com.dace.dmgr.combat.combatant.magritta.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatEntity;
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
        ShreddingValue shreddingValue = victim.getStatusEffectModule().apply(ValueStatusEffect.Type.SHREDDING, attacker,
                Timespan.ofTicks(MagrittaT1Info.DURATION));

        shreddingValue.addValue();
        if (shreddingValue.getValue() >= MagrittaT1Info.MAX) {
            victim.getStatusEffectModule().apply(MagrittaT1Burning.instance, attacker, Timespan.ofTicks(MagrittaT1Info.DURATION));

            MagrittaT1Info.SOUND.MAX.play(victim.getLocation());

            if (victim instanceof CombatUser)
                attacker.addScore("파쇄", MagrittaT1Info.MAX_DAMAGE_SCORE);
        }

        MagrittaT1Info.SOUND.USE.play(victim.getLocation());
    }

    /**
     * 파쇄 수치 상태 효과 클래스.
     */
    public static final class ShreddingValue extends ValueStatusEffect {
        /** 파쇄 수치 홀로그램 */
        @Nullable
        private TextHologram shreddingHologram;

        public ShreddingValue() {
            super(StatusEffectType.NONE, false, MagrittaT1Info.MAX);
        }

        private void addValue() {
            setValue(getValue() + 1);

            if (shreddingHologram != null)
                shreddingHologram.setContent(MessageFormat.format("§c{0} §f{1}", TextIcon.DAMAGE_INCREASE, getValue()));
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity.isCreature() && provider instanceof CombatUser)
                shreddingHologram = new TextHologram(combatEntity.getEntity(), target -> {
                    if (target == provider.getEntity())
                        return LocationUtil.canPass(target.getEyeLocation(), combatEntity.getCenterLocation());

                    return false;
                }, 1);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            setValue(0);
            if (shreddingHologram != null) {
                shreddingHologram.dispose();
                shreddingHologram = null;
            }
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
