package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.palas.Palas;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Healable;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;

public final class PalasA2 extends ActiveSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "PalasA2AssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "PalasA2";

    public PalasA2(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new PalasTarget().shoot();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 해로운 효과 면역 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PalasA2Immune implements StatusEffect {
        static final PalasA2Immune instance = new PalasA2Immune();

        @Override
        @NonNull
        public StatusEffectType getStatusEffectType() {
            return StatusEffectType.NONE;
        }

        @Override
        public boolean isPositive() {
            return true;
        }

        @Override
        public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getStatusEffectModule().getResistanceStatus().addModifier(MODIFIER_ID, 100);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatEntity.getCenterLocation(), 4,
                    1, 1.5, 1, 255, 230, 90);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getStatusEffectModule().getResistanceStatus().removeModifier(MODIFIER_ID);
        }
    }

    private final class PalasTarget extends Target {
        private PalasTarget() {
            super(combatUser, PalasA2Info.MAX_DISTANCE, true, combatEntity -> Palas.getTargetedActionCondition(PalasA2.this.combatUser, combatEntity) &&
                    !((Healable) combatEntity).getStatusEffectModule().hasStatusEffect(PalasA2Immune.instance));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            setCooldown();
            combatUser.getWeapon().onCancelled();

            target.getStatusEffectModule().removeStatusEffect(PalasUlt.PalasUltBuff.instance);
            target.getStatusEffectModule().clearStatusEffect(false);
            target.getStatusEffectModule().applyStatusEffect(combatUser, PalasA2Immune.instance, PalasA2Info.DURATION);
            if (target instanceof CombatUser) {
                ((CombatUser) target).getUser().sendTitle("§e§l해로운 효과 면역", "", 0, 5, 10);

                combatUser.addScore("해로운 효과 면역", PalasA2Info.USE_SCORE);
                ((CombatUser) target).addKillAssist(combatUser, PalasA2.ASSIST_SCORE_COOLDOWN_ID, PalasA2Info.ASSIST_SCORE, PalasA2Info.DURATION);
            }

            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A2_USE, combatUser.getEntity().getLocation());

            Location location = combatUser.getArmLocation(false);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4)) {
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 2, 0.1, 0.1, 0.1,
                        255, 230, 90);
                ParticleUtil.play(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
            }
            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A2_HIT_ENTITY, target.getCenterLocation());
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, target.getCenterLocation(), 40, 0.5, 0.5, 0.5, 0.2);
        }
    }
}
