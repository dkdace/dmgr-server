package com.dace.dmgr.combat.character.silia.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

public final class SiliaWeapon extends AbstractWeapon {
    /** 일격 사용 가능 여부 */
    @Getter
    private boolean isStrike = false;
    /** 검기 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    SiliaWeapon(@NonNull CombatUser combatUser) {
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
            if (!combatUser.getSkill(SiliaUltInfo.getInstance()).isDurationFinished())
                setCooldown(SiliaUltInfo.STRIKE_COOLDOWN);

            SiliaT2.strike(combatUser, isOpposite);
        } else {
            setCooldown();
            combatUser.playMeleeAttackAnimation(-4, 10, true);

            new SiliaWeaponProjectile().shoot();

            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_USE, combatUser.getEntity().getLocation());
        }

        if (!combatUser.getSkill(SiliaA3Info.getInstance()).isDurationFinished())
            combatUser.getSkill(SiliaA3Info.getInstance()).onCancelled();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setVisible(true);
    }

    /**
     * 일격 사용 가능 여부를 설정한다.
     *
     * @param isStrike 일격 사용 가능 여부
     */
    public void setStrike(boolean isStrike) {
        if (isStrike) {
            this.isStrike = true;
            combatUser.getWeapon().setGlowing(true);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.EXTENDED);
        } else {
            this.isStrike = false;
            combatUser.getWeapon().setGlowing(false);
            combatUser.getWeapon().displayDurability(SiliaWeaponInfo.RESOURCE.DEFAULT);
        }
    }

    private final class SiliaWeaponProjectile extends Projectile {
        private SiliaWeaponProjectile() {
            super(combatUser, SiliaWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(10).size(SiliaWeaponInfo.SIZE)
                    .maxDistance(SiliaWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
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
        protected void onHit() {
            ParticleUtil.play(Particle.EXPLOSION_NORMAL, location, 10, 0.1, 0.1, 0.1, 0.15);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            SoundUtil.playNamedSound(NamedSound.COMBAT_MELEE_ATTACK_HIT_BLOCK, location);
            CombatUtil.playBlockHitSound(location, hitBlock, 1);
            CombatUtil.playBlockHitEffect(location, hitBlock, 1.5);

            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location,
                    SiliaT1.isBackAttack(velocity, target) ? SiliaT1Info.CRIT_MULTIPLIER : 1, true);

            ParticleUtil.play(Particle.CRIT, location, 15, 0, 0, 0, 0.4);
            SoundUtil.playNamedSound(NamedSound.COMBAT_SILIA_WEAPON_HIT_ENTITY, location);

            return false;
        }
    }
}
