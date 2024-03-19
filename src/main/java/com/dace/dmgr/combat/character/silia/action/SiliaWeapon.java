package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class SiliaWeapon extends AbstractWeapon {
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
        setCooldown();
        isOpposite = !isOpposite;

        new SiliaWeaponProjectile().shoot();
        combatUser.playMeleeAttackAnimation(-4, 10);
        playUseSound(combatUser.getEntity().getLocation());
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
                Location loc = location.clone().add(vec);

                ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 2, 0.05, 0.05, 0.05,
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
            target.getDamageModule().damage(combatUser, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location, false, false);
            ParticleUtil.play(Particle.CRIT, location, 15, 0, 0, 0, 0.4);
            SoundUtil.play("random.stab", location, 1, 0.8, 0.05);

            return false;
        }
    }
}
