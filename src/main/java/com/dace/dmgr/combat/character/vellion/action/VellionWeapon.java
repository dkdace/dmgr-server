package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class VellionWeapon extends AbstractWeapon {
    public VellionWeapon(@NonNull CombatUser combatUser) {
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        combatUser.playMeleeAttackAnimation(-4, 8, true);

        new VellionWeaponProjectile().shoot();

        VellionWeaponInfo.SOUND.USE.play(combatUser.getEntity().getLocation());
    }

    private final class VellionWeaponProjectile extends Projectile {
        private VellionWeaponProjectile() {
            super(combatUser, VellionWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(12).size(VellionWeaponInfo.SIZE)
                    .maxDistance(VellionWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void onTrailInterval() {
            Location loc = LocationUtil.getLocationFromOffset(getLocation(), 0.2, -0.2, 0);
            VellionWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
        }

        @Override
        protected void onHit() {
            VellionWeaponInfo.PARTICLE.HIT.play(getLocation());
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            target.getDamageModule().damage(this, VellionWeaponInfo.DAMAGE, DamageType.NORMAL, getLocation(), isCrit, true);
            return false;
        }
    }
}
