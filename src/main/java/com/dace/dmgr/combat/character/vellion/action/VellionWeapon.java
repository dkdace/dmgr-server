package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;

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

        new VellionWeaponProjectile().shot();

        VellionWeaponInfo.SOUND.USE.play(combatUser.getEntity().getLocation());
    }

    private final class VellionWeaponProjectile extends Projectile<Damageable> {
        private VellionWeaponProjectile() {
            super(combatUser, VellionWeaponInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(VellionWeaponInfo.SIZE).maxDistance(VellionWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            VellionWeaponInfo.PARTICLE.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(12, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                VellionWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> false;
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                target.getDamageModule().damage(this, VellionWeaponInfo.DAMAGE, DamageType.NORMAL, location, isCrit, true);
                return false;
            });
        }
    }
}
