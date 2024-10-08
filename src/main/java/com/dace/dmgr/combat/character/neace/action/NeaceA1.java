package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.neace.Neace;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Target;
import com.dace.dmgr.util.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public final class NeaceA1 extends ActiveSkill {
    private final NeaceA1Mark neaceA1Mark = new NeaceA1Mark();

    public NeaceA1(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        new NeaceTarget().shoot();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    /**
     * 치유 표식 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NeaceA1Mark implements StatusEffect {
        private int healAmount = 0;

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
            healAmount = 0;
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() + 0.5, 0),
                    4, 0.2, 0.2, 0.2, 215, 255, 130);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() + 0.5, 0),
                    1, 0, 0, 0, 215, 255, 130);

            if (!(combatEntity instanceof Healable) || !(provider instanceof Healer))
                return;
            if (((Healable) combatEntity).getDamageModule().getHealth() == ((Healable) combatEntity).getDamageModule().getMaxHealth())
                return;

            if (healAmount >= NeaceA1Info.MAX_HEAL) {
                combatEntity.getStatusEffectModule().removeStatusEffect(this);
                return;
            }

            if (((Healable) combatEntity).getDamageModule().heal((Healer) provider, NeaceA1Info.HEAL_PER_SECOND / 20, true))
                healAmount += NeaceA1Info.HEAL_PER_SECOND / 20;
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }
    }

    private final class NeaceTarget extends Target {
        private NeaceTarget() {
            super(combatUser, NeaceA1Info.MAX_DISTANCE, true, combatEntity -> Neace.getTargetedActionCondition(NeaceA1.this.combatUser, combatEntity) &&
                    !((Healable) combatEntity).getStatusEffectModule().hasStatusEffect(neaceA1Mark));
        }

        @Override
        protected void onFindEntity(@NonNull Damageable target) {
            setCooldown();

            target.getStatusEffectModule().applyStatusEffect(combatUser, neaceA1Mark, NeaceA1Info.DURATION);

            SoundUtil.playNamedSound(NamedSound.COMBAT_NEACE_A1_USE, combatUser.getEntity().getLocation());
            playUseEffect(target);
        }

        private void playUseEffect(@NonNull Damageable target) {
            Location location = combatUser.getArmLocation(true);
            for (Location loc : LocationUtil.getLine(location, target.getCenterLocation(), 0.4)) {
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 2, 0.1, 0.1, 0.1,
                        215, 255, 130);
                ParticleUtil.play(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0);
            }

            Location location2 = LocationUtil.getLocationFromOffset(location, 0, 0, 1.5);
            Vector vector = VectorUtil.getYawAxis(location2).multiply(0.8);
            Vector axis = VectorUtil.getRollAxis(location2);

            for (int i = 0; i < 8; i++) {
                int angle = i * 10;

                for (int j = 0; j < 10; j++) {
                    angle += 72;
                    Vector vec = VectorUtil.getRotatedVector(vector, axis, j < 5 ? angle : -angle).multiply(1 + i * 0.2);

                    ParticleUtil.play(Particle.VILLAGER_HAPPY, location2.clone().add(vec), 2, 0, 0, 0, 0);
                }
            }
            for (int i = 0; i < 7; i++) {
                Location loc1 = LocationUtil.getLocationFromOffset(location2, -0.525 + i * 0.15, 0, 0);
                Location loc2 = LocationUtil.getLocationFromOffset(location2, 0, -0.525 + i * 0.15, 0);
                ParticleUtil.play(Particle.VILLAGER_HAPPY, loc1, 2, 0, 0, 0, 0);
                ParticleUtil.play(Particle.VILLAGER_HAPPY, loc2, 2, 0, 0, 0, 0);
            }
        }
    }
}
