package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
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
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

public final class ArkaceA1 extends ActiveSkill {
    public ArkaceA1(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceA1Info.getInstance(), 1);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SLOT_2, ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return ArkaceA1Info.COOLDOWN;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(ArkaceA1Info.GLOBAL_COOLDOWN);

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = combatUser.getArmLocation(false);
            new ArkaceA1Projectile().shoot(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_ARKACE_A1_USE, loc);

            return true;
        }, isCancelled -> TaskUtil.addTask(taskRunner, new DelayTask(this::onCancelled, 4)), 5, 3));
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

    private final class ArkaceA1Projectile extends Projectile {
        private ArkaceA1Projectile() {
            super(combatUser, ArkaceA1Info.VELOCITY, ProjectileOption.builder().trailInterval(10).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            ParticleUtil.play(Particle.CRIT_MAGIC, getLocation(), 1, 0, 0, 0, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, getLocation(), 1,
                    0, 0, 0, 32, 250, 225);
        }

        @Override
        protected void onHit() {
            Location loc = getLocation().clone().add(0, 0.1, 0);
            new ArkaceA1Area().emit(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_ARKACE_A1_EXPLODE, loc);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 200,
                    2.5, 2.5, 2.5, 32, 250, 225);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, loc, 40, 0.2, 0.2, 0.2, 0.2);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(this, ArkaceA1Info.DAMAGE_DIRECT, DamageType.NORMAL, getLocation(), false, true)
                    && target instanceof CombatUser)
                combatUser.addScore("미사일 직격", ArkaceA1Info.DIRECT_HIT_SCORE);

            return false;
        }

        private final class ArkaceA1Area extends Area {
            private ArkaceA1Area() {
                super(combatUser, ArkaceA1Info.RADIUS, ArkaceA1Projectile.this.condition.or(combatEntity -> combatEntity == ArkaceA1.this.combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);
                double damage = CombatUtil.getDistantDamage(ArkaceA1Info.DAMAGE_EXPLODE, distance, ArkaceA1Info.RADIUS / 2.0);
                if (target.getDamageModule().damage(ArkaceA1Projectile.this, damage, DamageType.NORMAL, null, false, true))
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(ArkaceA1Info.KNOCKBACK));

                return !(target instanceof Barrier);
            }
        }
    }
}
