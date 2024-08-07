package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Poison;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffect;
import com.dace.dmgr.combat.entity.module.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.entity.temporary.SummonEntity;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.FixedPitchHitbox;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.function.Predicate;

@Getter
public final class VellionA1 extends ActiveSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "VellionA1";
    /** 소환한 엔티티 */
    private VellionA1Entity summonEntity = null;

    VellionA1(@NonNull CombatUser combatUser) {
        super(combatUser, VellionA1Info.getInstance(), 0);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_1, ActionKey.RIGHT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return VellionA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !((VellionA3) combatUser.getSkill(VellionA3Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.setGlobalCooldown(VellionA1Info.GLOBAL_COOLDOWN);
        combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, -VellionA1Info.READY_SLOW);

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A1_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            playUseTickEffect(i);

            return true;
        }, isCancelled -> {
            onCancelled();

            Location loc = combatUser.getArmLocation(true);
            ArmorStand armorStand = CombatUtil.spawnEntity(ArmorStand.class, loc);
            summonEntity = new VellionA1Entity(armorStand, combatUser);
            summonEntity.activate();

            SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A1_USE_READY, loc);
        }, 1, VellionA1Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
    }

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 사용 시 효과를 재생한다.
     *
     * @param i 인덱스
     */
    private void playUseTickEffect(long i) {
        Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 1.5);
        Vector vector = VectorUtil.getYawAxis(loc);
        Vector axis = VectorUtil.getRollAxis(loc);

        for (int j = 0; j < i; j++) {
            int angle = j * 8;

            for (int k = 0; k < 12; k++) {
                angle += 60;
                Vector vec = VectorUtil.getRotatedVector(vector, axis, k < 6 ? angle : -angle).multiply(0.8 + j * 0.25);
                Location loc2 = loc.clone().add(vec);

                if (i == 9)
                    ParticleUtil.play(Particle.SPELL_WITCH, loc2, 1, 0, 0, 0, 0);
                else
                    ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc2, 1, 0, 0, 0,
                            (int) (45 + i * 8), 0, (int) (240 - i * 6));
            }
        }
    }

    /**
     * 회복 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class VellionA1Heal implements StatusEffect {
        private static final VellionA1Heal instance = new VellionA1Heal();

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
            // 미사용
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            if (combatEntity instanceof Healable && provider instanceof Healer)
                ((Healable) combatEntity).getDamageModule().heal((Healer) provider, VellionA1Info.HEAL_PER_SECOND / 20, true);
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            // 미사용
        }
    }

    /**
     * 독 상태 효과 클래스.
     */
    private static final class VellionA1Poison extends Poison {
        private static final VellionA1Poison instance = new VellionA1Poison();

        private VellionA1Poison() {
            super(VellionA1Info.POISON_DAMAGE_PER_SECOND);
        }
    }

    /**
     * 벨리온 - 마력 응집체 클래스.
     */
    public final class VellionA1Entity extends SummonEntity<ArmorStand> {
        private final HashSet<Damageable> targets = new HashSet<>();
        /** 회수 시간 */
        private long returnTime = VellionA1Info.RETURN_DURATION;

        private VellionA1Entity(@NonNull ArmorStand entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 마력 응집체",
                    owner,
                    true,
                    new FixedPitchHitbox(entity.getLocation(), 1, 1, 1, 0, 0.5, 0)
            );

            onInit();
        }

        private void onInit() {
            entity.setAI(false);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setMarker(true);
            entity.setVisible(false);
        }

        @Override
        protected void onTick(long i) {
            if (returnTime-- == 0)
                targets.clear();

            Location location = combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0);
            Vector dir = entity.getLocation().getDirection();
            if (returnTime < 0)
                dir = LocationUtil.getDirection(entity.getLocation(), location);
            Location loc = entity.getLocation().add(dir.multiply(VellionA1Info.VELOCITY / 20));

            if (LocationUtil.isNonSolid(loc)) {
                entity.teleport(loc);
                if (returnTime < 0 && (loc.distance(location) < 2 || combatUser.isDead()))
                    dispose();
            } else if (returnTime < -1)
                dispose();

            playTickEffect();

            Predicate<CombatEntity> condition = combatEntity -> combatEntity != combatUser && combatEntity instanceof Damageable &&
                    ((Damageable) combatEntity).getDamageModule().isLiving();
            CombatEntity[] areaTargets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, VellionA1Info.RADIUS, condition);
            new VellionA1Area(condition, areaTargets).emit(loc);
        }

        /**
         * 표시 효과를 재생한다.
         */
        private void playTickEffect() {
            Location loc = entity.getLocation();
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 20, 0.5, 0.5, 0.5,
                    120, 0, 220);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.FALLING_DUST, Material.WOOL, 10, loc, 8,
                    0.5, 0.5, 0.5, 0.05);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, loc, 10, 1.5, 1.5, 1.5,
                    140, 120, 180);
        }

        @Override
        public void dispose() {
            super.dispose();

            summonEntity = null;
        }

        private final class VellionA1Area extends Area {
            private VellionA1Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
                super(combatUser, VellionA1Info.RADIUS, condition, targets);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (targets.add(target)) {
                    if (target.isEnemy(combatUser)) {
                        if (target.getDamageModule().damage(combatUser, 0, DamageType.NORMAL, null,
                                false, true)) {
                            target.getStatusEffectModule().applyStatusEffect(combatUser, VellionA1Poison.instance,
                                    target.getStatusEffectModule().getStatusEffectDuration(VellionA1Poison.instance) + VellionA1Info.EFFECT_DURATION);
                            target.getStatusEffectModule().applyStatusEffect(combatUser, Snare.getInstance(), VellionA1Info.SNARE_DURATION);
                        }

                        ParticleUtil.play(Particle.CRIT_MAGIC, location, 30, 0, 0, 0, 0.4);
                        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_A1_HIT_ENTITY, location);
                    } else if (target instanceof Healable)
                        target.getStatusEffectModule().applyStatusEffect(combatUser, VellionA1Heal.instance,
                                target.getStatusEffectModule().getStatusEffectDuration(VellionA1Heal.instance) + VellionA1Info.EFFECT_DURATION);

                    if (target instanceof CombatUser)
                        combatUser.addScore("마력 집중", VellionA1Info.EFFECT_SCORE);
                }

                return true;
            }
        }
    }
}
