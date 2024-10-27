package com.dace.dmgr.combat.character.palas.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.character.palas.Palas;
import com.dace.dmgr.combat.entity.*;
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
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;

public final class PalasUlt extends UltimateSkill {
    /** 처치 지원 점수 제한시간 쿨타임 ID */
    public static final String ASSIST_SCORE_COOLDOWN_ID = "PalasUltAssistScoreTimeLimit";
    /** 수정자 ID */
    private static final String MODIFIER_ID = "PalasUlt";

    public PalasUlt(@NonNull CombatUser combatUser) {
        super(combatUser, PalasUltInfo.getInstance());
    }

    @Override
    public int getCost() {
        return PalasUltInfo.COST;
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
     * 아드레날린 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PalasUltBuff implements StatusEffect {
        static final PalasUltBuff instance = new PalasUltBuff();

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
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().addModifier(MODIFIER_ID, PalasUltInfo.DAMAGE_INCREMENT);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, PalasUltInfo.SPEED_INCREMENT);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatEntity.getCenterLocation(), 4,
                    1, 1.5, 1, 255, 70, 75);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatEntity.getCenterLocation(), 2,
                    1, 1.5, 1, 255, 50, 24);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Attacker)
                ((Attacker) combatEntity).getAttackModule().getDamageMultiplierStatus().removeModifier(MODIFIER_ID);
            if (combatEntity instanceof Movable)
                ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        }
    }

    private final class PalasTarget extends Target {
        private PalasTarget() {
            super(combatUser, PalasUltInfo.MAX_DISTANCE, true, combatEntity -> Palas.getTargetedActionCondition(PalasUlt.this.combatUser, combatEntity) &&
                    !((Healable) combatEntity).getStatusEffectModule().hasStatusEffect(PalasUltBuff.instance));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            PalasUlt.super.onUse(ActionKey.SLOT_4);

            setCooldown();
            combatUser.getWeapon().onCancelled();

            target.getStatusEffectModule().removeStatusEffect(PalasA2.PalasA2Immune.instance);
            target.getStatusEffectModule().applyStatusEffect(combatUser, PalasUltBuff.instance, PalasUltInfo.DURATION);
            if (target instanceof CombatUser) {
                ((CombatUser) target).getUser().sendTitle("§c§l아드레날린 투여", "", 0, 5, 10);

                combatUser.addScore("아군 강화", PalasUltInfo.USE_SCORE);
                ((CombatUser) target).addKillAssist(combatUser, PalasUlt.ASSIST_SCORE_COOLDOWN_ID, PalasUltInfo.ASSIST_SCORE, PalasUltInfo.DURATION);
            }

            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_ULT_USE, combatUser.getEntity().getLocation());

            Location location = combatUser.getArmLocation(false);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4)) {
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 2, 0.1, 0.1, 0.1,
                        255, 70, 75);
                ParticleUtil.play(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
            }
            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_ULT_HIT_ENTITY, target.getCenterLocation());
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, target.getCenterLocation(), 40, 0.5, 0.5, 0.5, 0.2);
            ParticleUtil.playFirework(target.getCenterLocation(), 255, 70, 75,
                    200, 0, 0, FireworkEffect.Type.BURST, false, false);
        }
    }
}
