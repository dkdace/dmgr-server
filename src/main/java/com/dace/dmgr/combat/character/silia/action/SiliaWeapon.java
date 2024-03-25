package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.character.silia.SiliaTrait;
import com.dace.dmgr.combat.entity.Barrier;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.combat.interaction.HitscanOption;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public final class SiliaWeapon extends AbstractWeapon {
    /** 일격 사용 가능 여부 */
    boolean isStrike = false;
    /** 검기 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public SiliaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, SiliaWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return SiliaWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse() {
        return super.canUse();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        isOpposite = !isOpposite;

        if (isStrike) {
            if (!combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
                combatUser.getSkill(SiliaA3Info.getInstance()).onCancelled();

            combatUser.setGlobalCooldown(6);
            setCooldown(6);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.USE);
            combatUser.playMeleeAttackAnimation(-2, 8, isOpposite);

            Set<CombatEntity> targets = new HashSet<>();

            int delay = 0;
            for (int i = 0; i < 8; i++) {
                final int index = i;

                switch (i) {
                    case 1:
                    case 2:
                    case 6:
                    case 7:
                        delay += 1;
                        break;
                }

                TaskUtil.addTask(taskRunner, new DelayTask(() -> {
                    Location loc = combatUser.getEntity().getEyeLocation();
                    Vector vector = VectorUtil.getPitchAxis(loc);
                    Vector axis = VectorUtil.getYawAxis(loc);

                    Vector vec = VectorUtil.getRotatedVector(vector, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);
                    axis = VectorUtil.getRotatedVector(axis, VectorUtil.getRollAxis(loc), isOpposite ? -30 : 30);

                    vec = VectorUtil.getRotatedVector(vec, axis, (isOpposite ? 90 - 16 * (index - 3.5) : 90 + 16 * (index - 3.5)));
                    new SiliaWeaponStrikeAttack(targets).shoot(loc, vec);
                    CombatUtil.addYawAndPitch(combatUser.getEntity(), (isOpposite ? -0.5 : 0.5), 0.15);

                    if (index < 3)
                        playStrikeSound(loc.add(vec), index);
                    if (index == 7) {
                        CombatUtil.addYawAndPitch(combatUser.getEntity(), isOpposite ? 0.7 : -0.7, -0.85);
                        onCancelled();
                    }
                }, delay));
            }
        } else {
            setCooldown();

            new SiliaWeaponProjectile().shoot();
            combatUser.playMeleeAttackAnimation(-4, 10, true);
            playUseSound(combatUser.getEntity().getLocation());
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
    }

    /**
     * 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     */
    private void playUseSound(Location location) {
        SoundUtil.play("random.gun2.knife_leftclick", location, 0.8, 1);
        SoundUtil.play("random.swordhit", location, 0.7, 1.2);
        SoundUtil.play("new.item.trident.riptide_1", location, 0.6, 1.3);
    }

    /**
     * 일격 사용 시 효과음을 재생한다.
     *
     * @param location 사용 위치
     * @param index    인덱스
     */
    private void playStrikeSound(Location location, int index) {
        SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_SWEEP, location, 1.5, 1);
        SoundUtil.play(Sound.ENTITY_IRONGOLEM_ATTACK, location, 1.5, 0.8);
        SoundUtil.play("random.swordhit", location, 1.5, 0.7 + index * 0.15);
    }

    private class SiliaWeaponProjectile extends Projectile {
        private SiliaWeaponProjectile() {
            super(SiliaWeapon.this.combatUser, SiliaWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(SiliaWeaponInfo.SIZE)
                    .maxDistance(SiliaWeaponInfo.DISTANCE).condition(SiliaWeapon.this.combatUser::isEnemy).build());
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            for (int i = 0; i < 8; i++) {
                Vector vector = VectorUtil.getYawAxis(location).multiply(-1);
                Vector axis = VectorUtil.getPitchAxis(location);
                Vector vec = VectorUtil.getRotatedVector(vector, VectorUtil.getRollAxis(location), isOpposite ? 30 : -30);
                axis = VectorUtil.getRotatedVector(axis, VectorUtil.getRollAxis(location), isOpposite ? 30 : -30);

                vec = VectorUtil.getRotatedVector(vec, axis, 90 + 20 * (i - 3.5)).multiply(0.8);
                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, location.clone().add(vec), 2, 0.05, 0.05, 0.05,
                        255, 255, 255);
            }
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 10, 0.1, 0.1, 0.1, 0.15);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            SoundUtil.play(Sound.ENTITY_PLAYER_ATTACK_WEAK, location, 0.8, 0.9, 0.05);
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                    7, 0.08, 0.08, 0.08, 0.1);
            ParticleUtil.play(Particle.TOWN_AURA, location, 40, 0.08, 0.08, 0.08, 0);
            SoundUtil.playBlockHitSound(location, hitBlock);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(combatUser, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location,
                    SiliaTrait.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
            ParticleUtil.play(Particle.CRIT, location, 15, 0, 0, 0, 0.4);
            SoundUtil.play("random.stab", location, 1, 0.8, 0.05);

            return false;
        }
    }

    private class SiliaWeaponStrikeAttack extends Hitscan {
        private final Set<CombatEntity> targets;

        public SiliaWeaponStrikeAttack(Set<CombatEntity> targets) {
            super(combatUser, HitscanOption.builder().trailInterval(5).size(0.5).maxDistance(SiliaA3Info.STRIKE.DISTANCE)
                    .condition(combatUser::isEnemy).build());

            this.targets = targets;
        }

        @Override
        protected void trail(@NonNull Location location, @NonNull Vector direction) {
            if (location.distance(combatUser.getEntity().getEyeLocation()) <= 1)
                return;

            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, trailLoc, 8, 0.15, 0.15, 0.15,
                    255, 255, 255);
        }

        @Override
        protected void onHit(@NonNull Location location) {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 3, 0.05, 0.05, 0.05, 0.05);
        }

        @Override
        protected boolean onHitBlock(@NonNull Location location, @NonNull Vector velocity, @NonNull Block hitBlock) {
            ParticleUtil.playBlock(ParticleUtil.BlockParticle.BLOCK_DUST, hitBlock.getType(), hitBlock.getData(), location,
                    7, 0.08, 0.08, 0.08, 0.1);
            ParticleUtil.play(Particle.TOWN_AURA, location, 40, 0.08, 0.08, 0.08, 0);
            SoundUtil.playBlockHitSound(location, hitBlock);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            if (targets.add(target)) {
                target.getDamageModule().damage(combatUser, SiliaA3Info.STRIKE.DAMAGE, DamageType.NORMAL, location,
                        SiliaTrait.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);
                target.getKnockbackModule().knockback(VectorUtil.getRollAxis(combatUser.getEntity().getLocation()));
                ParticleUtil.play(Particle.CRIT, location, 40, 0, 0, 0, 0.4);
                SoundUtil.play("random.stab", location, 1, 0.8, 0.05);
            }

            return !(target instanceof Barrier);
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location trailLoc = LocationUtil.getLocationFromOffset(location, 0, -0.3, 0);
            ParticleUtil.play(Particle.CRIT, trailLoc, 15, 0.08, 0.08, 0.08, 0.08);
        }
    }
}
