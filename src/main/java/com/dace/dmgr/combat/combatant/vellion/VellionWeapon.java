package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
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
import org.bukkit.inventory.MainHand;

public final class VellionWeapon extends AbstractWeapon {
    public VellionWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, VellionWeaponInfo.getInstance(), VellionWeaponInfo.COOLDOWN);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && !combatUser.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && combatUser.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        combatUser.playMeleeAttackAnimation(-4, Timespan.ofTicks(8), MainHand.RIGHT);

        new VellionWeaponProjectile().shot();

        VellionWeaponInfo.SOUND.USE.play(combatUser.getLocation());
    }

    private final class VellionWeaponProjectile extends Projectile<Damageable> {
        private VellionWeaponProjectile() {
            super(VellionWeapon.this, VellionWeaponInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
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
