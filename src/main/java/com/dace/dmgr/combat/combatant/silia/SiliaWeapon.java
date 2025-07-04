package com.dace.dmgr.combat.combatant.silia;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Projectile;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;

public final class SiliaWeapon extends AbstractWeapon implements LeftClickHandler {
    /** 검기 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public SiliaWeapon(@NonNull CombatUser combatUser, @NonNull SiliaWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, SiliaWeaponInfo.COOLDOWN);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(SiliaP2Info.getInstance()).isDurationFinished();
    }

    @Override
    public void onLeftClick() {
        isOpposite = !isOpposite;

        if (!combatUser.getAbilityManager().getAbility(SiliaT2Info.getInstance()).useStrike(isOpposite)) {
            setCooldown();
            combatUser.playMeleeAttackAnimation(-4, Timespan.ofTicks(10), MainHand.RIGHT);

            new SiliaWeaponProjectile().shot();

            SiliaWeaponInfo.Effects.USE.play(combatUser.getLocation());
        }

        combatUser.getAbilityManager().getAbility(SiliaA3Info.getInstance()).cancel();
    }

    @Override
    protected void onCancelled() {
        setVisible(true);
    }

    private final class SiliaWeaponProjectile extends Projectile<Damageable> {
        private SiliaWeaponProjectile() {
            super(SiliaWeapon.this, SiliaWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(SiliaWeaponInfo.SIZE).maxDistance(SiliaWeaponInfo.DISTANCE).build());
        }

        @Override
        protected void onHit(@NonNull Location location) {
            SiliaWeaponInfo.Effects.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(10, location -> SiliaWeaponInfo.Effects.playBulletTrail(location, isOpposite));
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                SiliaWeaponInfo.Effects.HIT_BLOCK.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                target.getDamageModule().damage(this, SiliaWeaponInfo.DAMAGE, DamageType.NORMAL, location,
                        SiliaT1.getCritMultiplier(getVelocity(), target), true);

                SiliaWeaponInfo.Effects.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
