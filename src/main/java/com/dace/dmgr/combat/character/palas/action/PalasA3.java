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
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;

@Getter
public final class PalasA3 extends ActiveSkill {
    public PalasA3(@NonNull CombatUser combatUser) {
        super(combatUser, PalasA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return PalasA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) PalasA3Info.READY_DURATION);

        SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A3_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getArmLocation(true), 0, 0, 0.3);
            new PalasA3Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_THROW, loc);
        }, PalasA3Info.READY_DURATION));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 체력 증가 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA3HealthIncrease implements StatusEffect {
        /** 증가한 최대 체력 */
        private int increasedHealth;

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
            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 + PalasA3Info.HEALTH_INCREASE_RATIO));
            increasedHealth = newMaxHealth - maxHealth;

            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);
            combatEntity.getDamageModule().setHealth(combatEntity.getDamageModule().getHealth() + increasedHealth);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§a§l최대 체력 증가", "", 0, 5, 10);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() - increasedHealth);
        }
    }

    /**
     * 체력 감소 상태 효과 클래스.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class PalasA3HealthDecrease implements StatusEffect {
        /** 감소한 최대 체력 */
        private int decreasedHealth;

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
            int maxHealth = combatEntity.getDamageModule().getMaxHealth();
            int newMaxHealth = (int) (maxHealth * (1 - PalasA3Info.HEALTH_DECREASE_RATIO));
            decreasedHealth = maxHealth - newMaxHealth;

            combatEntity.getDamageModule().setMaxHealth(newMaxHealth);
            if (combatEntity instanceof CombatUser)
                ((CombatUser) combatEntity).getUser().sendTitle("§c§l최대 체력 감소", "", 0, 5, 10);
        }

        @Override
        public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
            // 미사용
        }

        @Override
        public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
            combatEntity.getDamageModule().setMaxHealth(combatEntity.getDamageModule().getMaxHealth() + decreasedHealth);
        }
    }

    private final class PalasA3Projectile extends Projectile {
        private PalasA3Projectile() {
            super(combatUser, PalasA3Info.VELOCITY, ProjectileOption.builder().trailInterval(8).hasGravity(true).condition(combatEntity ->
                    Palas.getTargetedActionCondition(PalasA3.this.combatUser, combatEntity) || combatEntity.isEnemy(PalasA3.this.combatUser)).build());
        }

        @Override
        protected void onTrailInterval() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation(), 5,
                    0.15, 0.15, 0.15, 220, 161, 43);
        }

        @Override
        protected void onHit() {
            Location loc = getLocation().clone().add(0, 0.1, 0);
            new PalasA3Area().emit(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_PALAS_A3_EXPLODE, loc);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.STAINED_GLASS, 4, loc,
                    400, 0.1, 0.1, 0.1, 0.25);
            ParticleUtil.play(Particle.TOTEM, loc, 200, 0.15, 0.15, 0.15, 0.6);
            ParticleUtil.play(Particle.SPELL_INSTANT, loc, 300, 1.5, 1.5, 1.5, 1);
            ParticleUtil.play(Particle.WATER_SPLASH, loc, 300, 0.6, 0.6, 0.6, 0);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            return false;
        }

        private final class PalasA3Area extends Area {
            private PalasA3Area() {
                super(combatUser, PalasA3Info.RADIUS, combatEntity -> combatEntity instanceof Damageable &&
                        ((Damageable) combatEntity).getDamageModule().isLiving());
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                if (target.isEnemy(combatUser)) {
                    if (target.getDamageModule().damage(PalasA3Projectile.this, 1, DamageType.NORMAL, null,
                            false, true))
                        target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthDecrease(), PalasA3Info.DURATION);
                } else if (target instanceof Healable)
                    target.getStatusEffectModule().applyStatusEffect(combatUser, new PalasA3HealthIncrease(), PalasA3Info.DURATION);

                if (target instanceof CombatUser && target != combatUser)
                    combatUser.addScore("생체 제어 수류탄", PalasA3Info.EFFECT_SCORE);

                return !(target instanceof Barrier);
            }
        }
    }

}
