package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.AccessLevel;
import lombok.Getter;
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
        ShreddingValue shreddingValue = victim.getStatusEffectModule().get(ShreddingValue.class);
        if (shreddingValue == null)
            shreddingValue = new ShreddingValue(attacker);

        victim.getStatusEffectModule().apply(shreddingValue, MagrittaT1Info.DURATION);
        shreddingValue.shredding = Math.min(MagrittaT1Info.MAX, shreddingValue.shredding + 1);

        if (shreddingValue.shredding == MagrittaT1Info.MAX) {
            victim.getStatusEffectModule().apply(shreddingValue.burning, MagrittaT1Info.DURATION);

            MagrittaT1Info.Sounds.MAX.play(victim.getLocation());

            if (victim.isGoalTarget())
                attacker.addScore("파쇄", MagrittaT1Info.MAX_DAMAGE_SCORE);
        }

        MagrittaT1Info.Sounds.USE.play(victim.getLocation());
    }

    /**
     * 파쇄 수치 상태 효과 클래스.
     */
    public static final class ShreddingValue implements StatusEffect {
        /** 공격자 */
        private final CombatUser attacker;
        /** 화염 상태 효과 */
        private final Burning burning;
        /** 파쇄 수치 */
        @Getter(AccessLevel.PACKAGE)
        private int shredding = 0;
        /** 파쇄 수치 홀로그램 */
        @Nullable
        private TextHologram hologram;

        public ShreddingValue(@NonNull CombatUser attacker) {
            this.attacker = attacker;
            this.burning = new Burning(attacker, MagrittaT1Info.FIRE_DAMAGE_PER_SECOND, true);
        }

        @Override
        public boolean isPositive() {
            return false;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity) {
            if (hologram == null)
                hologram = new TextHologram(combatEntity.getEntity(), target -> {
                    if (target == attacker.getEntity())
                        return LocationUtil.canPass(target.getEyeLocation(), combatEntity.getCenterLocation());

                    return false;
                }, 2);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, long i) {
            if (hologram != null)
                hologram.setContent(MessageFormat.format("§c{0} §f{1}", TextIcon.DAMAGE_INCREASE, shredding));
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity) {
            if (hologram != null) {
                hologram.remove();
                hologram = null;
            }
        }
    }
}
