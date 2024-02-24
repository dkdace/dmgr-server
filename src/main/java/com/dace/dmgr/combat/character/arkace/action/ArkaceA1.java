package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.ActiveSkill;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public final class ArkaceA1 extends ActiveSkill {
    public ArkaceA1(@NonNull CombatUser combatUser) {
        super(1, combatUser, ArkaceA1Info.getInstance(), 1);
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        combatUser.getWeapon().onCancelled();
        combatUser.setGlobalCooldown(10);
        setDuration();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            Location loc = LocationUtil.getLocationFromOffset(combatUser.getEntity().getEyeLocation().subtract(0, 0.4, 0),
                    combatUser.getEntity().getLocation().getDirection(), -0.2, 0, 0);
            new ArkaceA1Projectile().shoot(loc);
            playShootSound(loc);

            return true;
        }, isCancelled ->
                TaskUtil.addTask(ArkaceA1.this, new DelayTask(this::onCancelled, 4)), 5, 3));
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }

    /**
     * 발사 시 효과음을 재생한다.
     *
     * @param location 발사 위치
     */
    private void playShootSound(Location location) {
        SoundUtil.play("random.gun.grenade", location, 3, 1.5);
        SoundUtil.play(Sound.ENTITY_SHULKER_SHOOT, location, 3, 1.2);
    }

    private class ArkaceA1Projectile extends Projectile {
        private ArkaceA1Projectile() {
            super(ArkaceA1.this.combatUser, ArkaceA1Info.VELOCITY, ProjectileOption.builder().trailInterval(10)
                    .condition(ArkaceA1.this.combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location) {
            ParticleUtil.play(Particle.CRIT_MAGIC, location, 1, 0, 0, 0, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 1,
                    0, 0, 0, 32, 250, 225);
        }

        @Override
        protected void onHit(@NonNull Location location) {
            explode(location.add(0, 0.1, 0));
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector direction, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector direction, @NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, ArkaceA1Info.DAMAGE_DIRECT, DamageType.NORMAL, false, true);
            return false;
        }

        private void explode(Location location) {
            Predicate<CombatEntity> condition = combatEntity -> combatEntity instanceof Damageable &&
                    (combatEntity.isEnemy(combatUser) || combatEntity == combatUser);
            CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), location, ArkaceA1Info.RADIUS, condition);

            new ArkaceA1Area(condition, targets).emit(location);
            playExplodeEffect(location);
        }

        private void playExplodeEffect(Location location) {
            SoundUtil.play(Sound.ENTITY_FIREWORK_LARGE_BLAST, location, 4, 0.8);
            SoundUtil.play(Sound.ENTITY_GENERIC_EXPLODE, location, 4, 1.4);
            SoundUtil.play("random.gun_reverb2", location, 6, 0.9);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location, 200,
                    2.5, 2.5, 2.5, 32, 250, 225);
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 40, 0.2, 0.2, 0.2, 0.2);
        }
    }

    private class ArkaceA1Area extends Area {
        private ArkaceA1Area(Predicate<CombatEntity> condition, CombatEntity[] targets) {
            super(ArkaceA1.this.combatUser, ArkaceA1Info.RADIUS, condition, targets);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
            return false;
        }

        @Override
        public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
            target.getDamageModule().damage(combatUser, ArkaceA1Info.DAMAGE_EXPLODE, DamageType.NORMAL, false, true);
            return !(target instanceof Barrier);
        }
    }
}
