package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Snare;
import com.dace.dmgr.combat.entity.temporal.SummonEntity;
import com.dace.dmgr.combat.interaction.*;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.MagmaCube;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Getter
public final class JagerA2 extends ActiveSkill {
    /** 소환한 엔티티 */
    private JagerA2Entity summonEntity = null;

    JagerA2(@NonNull CombatUser combatUser) {
        super(combatUser, JagerA2Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA2Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && isDurationFinished() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(JagerA3Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown((int) JagerA2Info.READY_DURATION);
        if (summonEntity != null)
            summonEntity.dispose();

        SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_USE, combatUser.getEntity().getLocation());

        TaskUtil.addTask(taskRunner, new DelayTask(() -> {
            onCancelled();

            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            new JagerA2Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_THROW, loc);
        }, JagerA2Info.READY_DURATION));
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

    @Override
    public void reset() {
        super.reset();

        if (summonEntity != null)
            summonEntity.dispose();
    }

    /**
     * 예거 - 곰덫 클래스.
     */
    @Getter
    public final class JagerA2Entity extends SummonEntity<MagmaCube> implements HasReadyTime, Damageable, Attacker {
        /** 넉백 모듈 */
        @NonNull
        private final KnockbackModule knockbackModule;
        /** 상태 효과 모듈 */
        @NonNull
        private final StatusEffectModule statusEffectModule;
        /** 공격 모듈 */
        @NonNull
        private final AttackModule attackModule;
        /** 피해 모듈 */
        @NonNull
        private final DamageModule damageModule;
        /** 준비 시간 모듈 */
        @NonNull
        private final ReadyTimeModule readyTimeModule;

        private JagerA2Entity(@NonNull MagmaCube entity, @NonNull CombatUser owner) {
            super(
                    entity,
                    owner.getName() + "의 곰덫",
                    owner,
                    true,
                    new FixedPitchHitbox(entity.getLocation(), 0.8, 0.1, 0.8, 0, 0.05, 0)
            );
            knockbackModule = new KnockbackModule(this, 2);
            statusEffectModule = new StatusEffectModule(this, 2);
            attackModule = new AttackModule(this);
            damageModule = new DamageModule(this, false, true, JagerA2Info.HEALTH);
            readyTimeModule = new ReadyTimeModule(this, JagerA2Info.SUMMON_DURATION);

            onInit();
        }

        private void onInit() {
            entity.setAI(false);
            entity.setSize(1);
            entity.setSilent(true);
            entity.setInvulnerable(true);
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false), true);
            entity.teleport(entity.getLocation().add(0, 0.05, 0));
            damageModule.setMaxHealth(JagerA2Info.HEALTH);
            damageModule.setHealth(JagerA2Info.HEALTH);

            GlowUtil.setGlowing(entity, ChatColor.WHITE, owner.getEntity());
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_SUMMON, entity.getLocation());
        }

        @Override
        public void activate() {
            super.activate();
            readyTimeModule.ready();
        }

        @Override
        public void onTickBeforeReady(long i) {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB, entity.getLocation(), 5, 0.2, 0.2, 0.2,
                    120, 120, 135);
            playTickEffect();
        }

        @Override
        public void onReady() {
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_SUMMON_READY, entity.getLocation());
        }

        @Override
        protected void onTick(long i) {
            if (!readyTimeModule.isReady())
                return;

            Damageable target = (Damageable) CombatUtil.getNearCombatEntity(game, entity.getLocation(), 0.8,
                    combatEntity -> combatEntity instanceof Damageable && combatEntity instanceof Living && combatEntity.isEnemy(this));
            if (target != null)
                onCatchEnemy(target);

            playTickEffect();
        }

        /**
         * 덫 표시 효과를 재생한다.
         */
        private void playTickEffect() {
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, 0.6), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, 0.4), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, 0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, 0), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, -0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.55, 0, -0.4), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0.4, 0, -0.6), 1, 0, 0, 0, 0);

            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, 0.6), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, 0.4), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, 0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, 0), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, -0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.55, 0, -0.4), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(-0.4, 0, -0.6), 1, 0, 0, 0, 0);

            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0.4), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, 0), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, -0.2), 1, 0, 0, 0, 0);
            ParticleUtil.play(Particle.TOWN_AURA, entity.getLocation().add(0, 0, -0.4), 1, 0, 0, 0, 0);
        }

        /**
         * 덫 발동 시 실행할 작업.
         *
         * @param target 대상 엔티티
         */
        private void onCatchEnemy(@NonNull Damageable target) {
            if (!target.getDamageModule().damage(this, JagerA2Info.DAMAGE, DamageType.NORMAL, target.getEntity().getLocation().add(0, 0.2, 0),
                    false, true))
                return;

            target.getStatusEffectModule().applyStatusEffect(this, Snare.getInstance(), JagerA2Info.SNARE_DURATION);
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_TRIGGER, entity.getLocation());
            if (target instanceof CombatUser)
                combatUser.addScore("곰덫", JagerA2Info.SNARE_SCORE);

            dispose();
        }

        @Override
        public void dispose() {
            super.dispose();

            summonEntity = null;
        }

        @Override
        public void onAttack(@NonNull Damageable victim, int damage, @NonNull DamageType damageType, boolean isCrit, boolean isUlt) {
            owner.onAttack(victim, damage, damageType, isCrit, isUlt);

            JagerA1 skill1 = (JagerA1) owner.getSkill(JagerA1Info.getInstance());
            if (skill1.getSummonEntity() != null && skill1.getSummonEntity().getEntity().getTarget() == null)
                skill1.getSummonEntity().getEntity().setTarget(victim.getEntity());
        }

        @Override
        public void onKill(@NonNull Damageable victim) {
            owner.onKill(victim);
        }

        @Override
        public void onDamage(@Nullable Attacker attacker, int damage, int reducedDamage, @NonNull DamageType damageType, @Nullable Location location, boolean isCrit, boolean isUlt) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_DAMAGE, entity.getLocation(), 1 + damage * 0.001);
            CombatUtil.playBreakEffect(location, entity, damage);
        }

        @Override
        public void onDeath(@Nullable Attacker attacker) {
            dispose();

            if (attacker instanceof CombatUser)
                ((CombatUser) attacker).addScore("§e" + name + " §f파괴", JagerA2Info.DEATH_SCORE);

            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.IRON_BLOCK, 0, entity.getLocation(), 80,
                    0.1, 0.1, 0.1, 0.15);
            SoundUtil.playNamedSound(NamedSound.COMBAT_JAGER_A2_DEATH, entity.getLocation());
        }
    }

    private final class JagerA2Projectile extends BouncingProjectile {
        private JagerA2Projectile() {
            super(combatUser, JagerA2Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8).hasGravity(true)
                    .condition(combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35)
                    .destroyOnHitFloor(true).build());
        }

        @Override
        protected void trail() {
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 17,
                    0.7, 0, 0.7, 120, 120, 135);
        }

        @Override
        protected boolean onHitBlockBouncing(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntityBouncing(@NonNull Damageable target, boolean isCrit) {
            return false;
        }

        @Override
        protected void onDestroy() {
            MagmaCube magmaCube = CombatUtil.spawnEntity(MagmaCube.class, location);
            summonEntity = new JagerA2Entity(magmaCube, combatUser);
            summonEntity.activate();
        }
    }
}
