package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.ParticleUtil;
import com.dace.dmgr.util.SoundUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;

public final class VellionWeapon extends AbstractWeapon {
    VellionWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, VellionWeaponInfo.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public long getDefaultCooldown() {
        return VellionWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !((VellionA3) combatUser.getSkill(VellionA3Info.getInstance())).getConfirmModule().isChecking() &&
                combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        combatUser.playMeleeAttackAnimation(-4, 8, true);

        new VellionWeaponProjectile().shoot();

        SoundUtil.playNamedSound(NamedSound.COMBAT_VELLION_WEAPON_USE, combatUser.getEntity().getLocation());
    }

    private final class VellionWeaponProjectile extends Projectile {
        private VellionWeaponProjectile() {
            super(combatUser, VellionWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(12).size(VellionWeaponInfo.SIZE)
                    .maxDistance(VellionWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            ParticleUtil.play(Particle.SPELL_WITCH, loc, 4, 0.1, 0.1, 0.1, 0);
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.REDSTONE, loc, 6, 0.25, 0.25, 0.25,
                    80, 30, 110);
        }

        @Override
        protected void onHit() {
            ParticleUtil.play(Particle.SMOKE_NORMAL, location, 30, 0.1, 0.1, 0.1, 0.1);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, VellionWeaponInfo.DAMAGE, DamageType.NORMAL, location, isCrit, true);
            return false;
        }
    }
}
