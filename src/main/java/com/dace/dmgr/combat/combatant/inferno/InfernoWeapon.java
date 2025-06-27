package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Set;

public final class InfernoWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 화염 상태 효과 */
    private final Burning burning;

    public InfernoWeapon(@NonNull CombatUser combatUser, @NonNull InfernoWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, InfernoWeaponInfo.Fireball.COOLDOWN);

        this.reloadModule = new ReloadModule(this, InfernoWeaponInfo.CAPACITY, InfernoWeaponInfo.RELOAD_DURATION);
        this.fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
        this.burning = new Burning(combatUser, InfernoWeaponInfo.FIRE_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.RIGHT_CLICK, ActionKey.LEFT_CLICK, ActionKey.DROP);
    }

    @Override
    @NonNull
    protected Set<@NonNull ActionKey> getCooldownIgnoreActionKeys() {
        return EnumSet.of(ActionKey.DROP);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(10, StringFormUtil.PROGRESS_DEFAULT_SYMBOL).build();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                new InfernoWeaponRProjectile().shot(VectorUtil.getSpreadedVector(combatUser.getLocation().getDirection(), InfernoWeaponInfo.SPREAD));

                if (combatUser.getAbilityManager().getAbility(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(1);

                InfernoWeaponInfo.Effects.USE.play(combatUser.getLocation());

                break;
            }
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() < InfernoWeaponInfo.Fireball.CAPACITY_CONSUME) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                new InfernoWeaponLProjectile().shot();

                if (combatUser.getAbilityManager().getAbility(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(InfernoWeaponInfo.Fireball.CAPACITY_CONSUME);

                InfernoWeaponInfo.Fireball.RECOIL.send(combatUser);
                InfernoWeaponInfo.Effects.FIREBALL_USE.play(combatUser.getLocation());

                break;
            }
            case DROP: {
                onAmmoEmpty();
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected void onCancelled() {
        reloadModule.cancel();
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < InfernoWeaponInfo.CAPACITY;
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        cancel();
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        InfernoWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    private final class InfernoWeaponRProjectile extends Projectile<Damageable> {
        private InfernoWeaponRProjectile() {
            super(InfernoWeapon.this, InfernoWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(InfernoWeaponInfo.SIZE).maxDistance(InfernoWeaponInfo.DISTANCE).build());
        }

        @Override
        protected boolean canBeRemoved() {
            return false;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(9, location -> {
                double distance = getTravelDistance();
                if (distance > 5)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                InfernoWeaponInfo.Effects.BULLET_TRAIL.apply(getVelocity(), distance).play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                InfernoWeaponInfo.Effects.HIT_BLOCK.play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.DAMAGE_PER_SECOND / 20.0, DamageType.NORMAL, null,
                        false, true))
                    target.getStatusEffectModule().apply(burning, InfernoWeaponInfo.FIRE_DURATION);

                InfernoWeaponInfo.Effects.HIT_ENTITY.play(location);
                return true;
            };
        }
    }

    private final class InfernoWeaponLProjectile extends Projectile<Damageable> {
        private InfernoWeaponLProjectile() {
            super(InfernoWeapon.this, InfernoWeaponInfo.Fireball.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(InfernoWeaponInfo.Fireball.SIZE).maxDistance(InfernoWeaponInfo.Fireball.DISTANCE).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location, boolean isForce) {
            if (isForce)
                return;

            Location loc = location.add(0, 0.1, 0);
            new InfernoWeaponLArea().emit(loc);

            InfernoWeaponInfo.Effects.FIREBALL_EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                InfernoWeaponInfo.Effects.FIREBALL_BULLET_TRAIL.play(loc);
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
            return (location, target) -> {
                if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.Fireball.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true)
                        && target instanceof Movable) {
                    Vector dir = getVelocity().normalize().multiply(InfernoWeaponInfo.Fireball.KNOCKBACK);
                    ((Movable) target).getKnockbackModule().knockback(dir);
                }

                return false;
            };
        }

        private final class InfernoWeaponLArea extends Area<Damageable> {
            private InfernoWeaponLArea() {
                super(combatUser, InfernoWeaponInfo.Fireball.RADIUS, InfernoWeaponLProjectile.this.entityCondition.include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);

                if (target.getDamageModule().damage(InfernoWeaponLProjectile.this,
                        InfernoWeaponInfo.Fireball.DISTANT_DAMAGE_EXPLODE.getDamage(distance), DamageType.NORMAL, null, false, true)) {
                    target.getStatusEffectModule().apply(burning, InfernoWeaponInfo.Fireball.DISTANT_FIRE_DURATION.getTimespan(distance));

                    if (target instanceof Movable && !InfernoWeaponLProjectile.this.getHitTargets().contains(target)) {
                        Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(InfernoWeaponInfo.Fireball.KNOCKBACK);
                        ((Movable) target).getKnockbackModule().knockback(dir);
                    }
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
