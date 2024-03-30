package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.character.silia.SiliaTrait;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

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
        return super.canUse() && combatUser.getSkill(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        isOpposite = !isOpposite;

        if (isStrike) {
            setCooldown(6);
            SiliaTrait.strike(combatUser, isOpposite);
        } else {
            setCooldown();

            new SiliaWeaponProjectile().shoot();
            combatUser.playMeleeAttackAnimation(-4, 10, true);
            SoundUtil.play(NamedSound.COMBAT_SILIA_WEAPON_USE, combatUser.getEntity().getLocation());
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setVisible(true);
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
            SoundUtil.play(NamedSound.COMBAT_MELEE_ATTACK_HIT_BLOCK, location);
            SoundUtil.playBlockHitSound(location, hitBlock, 1);
            ParticleUtil.playBlockHitEffect(location, hitBlock, 1.5);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Location location, @NonNull Vector velocity, @NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(combatUser, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location,
                    SiliaTrait.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);

            ParticleUtil.play(Particle.CRIT, location, 15, 0, 0, 0, 0.4);
            SoundUtil.play(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, location);

            return false;
        }
    }
}
