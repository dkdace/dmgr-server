package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

import java.util.EnumSet;
import java.util.Set;

public final class VellionWeapon extends AbstractWeapon {
    public VellionWeapon(@NonNull CombatUser combatUser, @NonNull VellionWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, VellionWeaponInfo.COOLDOWN);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.LEFT_CLICK);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse(actionKey) && !abilityManager.getSkill(VellionA3Info.getInstance()).getConfirmModule().isChecking()
                && abilityManager.getSkill(VellionUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();
        combatUser.playMeleeAttackAnimation(-4, Timespan.ofTicks(8), MainHand.RIGHT);

        new VellionWeaponProjectile().shot();

        VellionWeaponInfo.Effects.USE.play(combatUser.getLocation());
    }

    private final class VellionWeaponProjectile extends Projectile<Damageable> {
        private VellionWeaponProjectile() {
            super(VellionWeapon.this, VellionWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(VellionWeaponInfo.SIZE).maxDistance(VellionWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            VellionWeaponInfo.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(12, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                VellionWeaponInfo.Effects.BULLET_TRAIL.play(loc);
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
