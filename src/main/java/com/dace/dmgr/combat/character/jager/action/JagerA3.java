package com.dace.dmgr.combat.character.jager.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.character.jager.JagerTrait;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.statuseffect.StatusEffectType;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.BouncingProjectile;
import com.dace.dmgr.combat.interaction.BouncingProjectileOption;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.*;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public final class JagerA3 extends ActiveSkill {
    /** 수류탄 활성화 완료 여부 */
    private boolean isEnabled = false;

    public JagerA3(@NonNull CombatUser combatUser) {
        super(3, combatUser, JagerA3Info.getInstance(), 2);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_3};
    }

    @Override
    public long getDefaultCooldown() {
        return JagerA3Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !((JagerA1) combatUser.getSkill(JagerA1Info.getInstance())).getConfirmModule().isChecking();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();

        if (isDurationFinished()) {
            combatUser.setGlobalCooldown((int) JagerA3Info.READY_DURATION);
            setDuration();
            SoundUtil.play(NamedSound.COMBAT_JAGER_A3_USE, combatUser.getEntity().getLocation());

            TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                CooldownUtil.setCooldown(combatUser, Cooldown.JAGER_A3_EXPLODE_DURATION);
                SoundUtil.play(NamedSound.COMBAT_JAGER_A3_USE_READY, combatUser.getEntity().getLocation());
                isEnabled = true;

                TaskUtil.addTask(JagerA3.this, new IntervalTask(i -> {
                    if (isDurationFinished())
                        return false;

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                            combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                    playTickEffect(loc);

                    return true;
                }, isCancelled -> {
                    if (isCancelled)
                        return;

                    Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                            combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0.3);
                    isEnabled = false;
                    onCancelled();

                    explode(loc, null);
                }, 1, JagerA3Info.EXPLODE_DURATION));
            }, JagerA3Info.READY_DURATION));
        } else {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), 0.2, 0, 0);
            new JagerA3Projectile().shoot(loc);
            SoundUtil.play(NamedSound.COMBAT_THROW, loc);

            isEnabled = false;
            onCancelled();
        }
    }

    @Override
    public void onCancelled() {
        if (!isEnabled) {
            super.onCancelled();
            setDuration(0);
        }
    }

    /**
     * 사용 중 효과를 재생한다.
     *
     * @param location 사용 위치
     */
    private void playTickEffect(Location location) {
        ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 3, 0.1, 0.1, 0.1, 120, 220, 240);
    }

    /**
     * 폭파 이벤트를 호출한다.
     *
     * @param location   폭파 위치
     * @param projectile 투사체
     */
    private void explode(Location location, JagerA3Projectile projectile) {
        Predicate<CombatEntity> condition = combatEntity -> combatEntity instanceof Damageable &&
                (combatEntity.isEnemy(combatUser) || combatEntity == combatUser);
        CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), location, JagerA3Info.RADIUS, condition);

        new JagerA3Area(condition, targets, projectile).emit(location);

        SoundUtil.play(NamedSound.COMBAT_JAGER_A3_EXPLODE, location);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.ICE, 0, location,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, Material.PACKED_ICE, 0, location,
                300, 0.2, 0.2, 0.2, 0.5);
        ParticleUtil.play(Particle.EXPLOSION_LARGE, location, 1, 0, 0, 0, 0);
        ParticleUtil.play(Particle.FIREWORKS_SPARK, location, 200, 0, 0, 0, 0.3);
    }

    private class JagerA3Projectile extends BouncingProjectile {
        private JagerA3Projectile() {
            super(JagerA3.this.combatUser, JagerA3Info.VELOCITY, -1, ProjectileOption.builder().trailInterval(8)
                    .duration(CooldownUtil.getCooldown(JagerA3.this.combatUser, Cooldown.JAGER_A3_EXPLODE_DURATION)).hasGravity(true)
                    .condition(JagerA3.this.combatUser::isEnemy).build(), BouncingProjectileOption.builder().bounceVelocityMultiplier(0.35).build());
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            playTickEffect(location);
        }

        @Override
        protected boolean onHitBlockBouncing(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            SoundUtil.play(NamedSound.COMBAT_THROW_BOUNCE, location, 0.1 + velocity.length() * 2);
            return false;
        }

        @Override
        protected boolean onHitEntityBouncing(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            if (velocity.length() > 0.05)
                target.getDamageModule().damage(this, JagerA3Info.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true);
            return false;
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            explode(location.add(0, 0.1, 0), this);
        }
    }

    private class JagerA3Area extends Area {
        private final JagerA3Projectile projectile;

        private JagerA3Area(Predicate<CombatEntity> condition, CombatEntity[] targets, JagerA3Projectile projectile) {
            super(JagerA3.this.combatUser, JagerA3Info.RADIUS, condition, targets);
            this.projectile = projectile;
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            int damage = CombatUtil.getDistantDamage(center, target.getEntity().getLocation(), JagerA3Info.DAMAGE_EXPLODE,
                    JagerA3Info.RADIUS / 2.0, true);
            int freeze = CombatUtil.getDistantDamage(center, target.getEntity().getLocation(), JagerA3Info.FREEZE,
                    JagerA3Info.RADIUS / 2.0, true);
            if (projectile == null)
                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, null, false, true);
            else
                target.getDamageModule().damage(projectile, damage, DamageType.NORMAL, null, false, true);
            target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(0.6));
            JagerTrait.addFreezeValue(target, freeze);

            if (target.getPropertyManager().getValue(Property.FREEZE) >= JagerT1Info.MAX)
                target.getStatusEffectModule().applyStatusEffect(StatusEffectType.SNARE, JagerTrait.Freeze.getInstance(), JagerA3Info.SNARE_DURATION);

            return !(target instanceof Barrier);
        }
    }
}
