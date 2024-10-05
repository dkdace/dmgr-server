package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.action.TextIcon;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.util.HologramUtil;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public final class MagrittaT1 {
    /** 홀로그램 ID */
    private static final String HOLOGRAM_ID = "Shredding";

    /**
     * 피격자의 파쇄 수치를 증가시킨다.
     *
     * @param attacker 공격자
     * @param victim   피격자
     */
    static void addShreddingValue(@NonNull CombatUser attacker, @NonNull Damageable victim) {
        victim.getPropertyManager().addValue(Property.SHREDDING, 1);
        victim.getStatusEffectModule().applyStatusEffect(attacker, ShreddingValue.instance, MagrittaT1Info.DURATION);
        if (victim.getPropertyManager().getValue(Property.SHREDDING) >= MagrittaT1Info.MAX) {
            victim.getStatusEffectModule().applyStatusEffect(attacker, MagrittaT1Burning.instance, MagrittaT1Info.DURATION);
            if (victim instanceof CombatUser)
                attacker.addScore("파쇄", MagrittaT1Info.MAX_DAMAGE_SCORE);

            SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_T1_MAX, victim.getEntity().getLocation());
        }

        SoundUtil.playNamedSound(NamedSound.COMBAT_MAGRITTA_T1_USE, victim.getEntity().getLocation());
    }

    /**
     * 파쇄 수치 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ShreddingValue implements StatusEffect {
        private static final ShreddingValue instance = new ShreddingValue();

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
            if (!combatEntity.getDamageModule().isLiving())
                return;

            HologramUtil.addHologram(HOLOGRAM_ID + combatEntity, combatEntity.getEntity(),
                    0, combatEntity.getEntity().getHeight() + 0.7, 0, "§f");
            HologramUtil.setHologramVisibility(HOLOGRAM_ID + combatEntity, false, Bukkit.getOnlinePlayers().toArray(new Player[0]));
            HologramUtil.setHologramVisibility(HOLOGRAM_ID + combatEntity, true, provider.getEntity());
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (!combatEntity.getDamageModule().isLiving())
                return;

            HologramUtil.editHologram(HOLOGRAM_ID + combatEntity, "§c" + TextIcon.DAMAGE_INCREASE + " §f" +
                    combatEntity.getPropertyManager().getValue(Property.SHREDDING));
            if (provider instanceof CombatUser)
                HologramUtil.setHologramVisibility(HOLOGRAM_ID + combatEntity,
                        LocationUtil.canPass(((CombatUser) provider).getEntity().getEyeLocation(), combatEntity.getCenterLocation()), provider.getEntity());
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getPropertyManager().setValue(Property.SHREDDING, 0);

            if (combatEntity.getDamageModule().isLiving())
                HologramUtil.removeHologram(HOLOGRAM_ID + combatEntity);
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
