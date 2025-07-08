package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.KnockbackModule;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;

public final class InfernoWeapon extends AbstractWeapon implements Reloadable, FullAuto, LeftClickHandler, DropHandler {
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

        this.reloadModule = new ReloadModule(this);
        this.fullAutoModule = new FullAutoModule(this);
        this.burning = new Burning(combatUser, InfernoWeaponInfo.FIRE_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(10, StringFormUtil.PROGRESS_DEFAULT_SYMBOL).build();
    }

    @Override
    public void onRightClick() {
        if (combatUser.getAbilityManager().getAbility(InfernoUltInfo.getInstance()).isDurationFinished() && !reloadModule.consume(1))
            return;

        new InfernoWeaponRProjectile().shot();

        InfernoWeaponInfo.Effects.USE.play(combatUser.getLocation());
    }

    @Override
    public void onLeftClick() {
        if (combatUser.getAbilityManager().getAbility(InfernoUltInfo.getInstance()).isDurationFinished()
                && !reloadModule.consume(InfernoWeaponInfo.Fireball.CAPACITY_CONSUME))
            return;

        setCooldown();

        new InfernoWeaponLProjectile().shot();

        InfernoWeaponInfo.Fireball.RECOIL.send(combatUser);
        InfernoWeaponInfo.Effects.FIREBALL_USE.play(combatUser.getLocation());
    }

    @Override
    public boolean isDropIgnoreCooldown() {
        return true;
    }

    @Override
    public void onDrop() {
        reloadModule.reload();
    }

    @Override
    protected void onCancelled() {
        reloadModule.cancel();
    }

    @Override
    public int getCapacity() {
        return InfernoWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return InfernoWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onAmmoEmpty() {
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

    @Override
    @NonNull
    public FireRate getFireRate() {
        return FireRate.RPM_1200;
    }

    private final class InfernoWeaponRProjectile extends Projectile<Damageable> {
        private InfernoWeaponRProjectile() {
            super(InfernoWeapon.this, InfernoWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().size(InfernoWeaponInfo.SIZE).maxDistance(InfernoWeaponInfo.DISTANCE).spread(InfernoWeaponInfo.SPREAD).build());
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
                if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.Fireball.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true))
                    KnockbackModule.knockback(target, getVelocity(), InfernoWeaponInfo.Fireball.KNOCKBACK);

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

                    if (!InfernoWeaponLProjectile.this.getHitTargets().contains(target))
                        KnockbackModule.knockback(target, LocationUtil.getDirection(center, location.clone().add(0, 0.5, 0)),
                                InfernoWeaponInfo.Fireball.KNOCKBACK);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
