package com.dace.dmgr.combat.combatant.inferno;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

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

    public InfernoWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoWeaponInfo.getInstance(), InfernoWeaponInfo.Fireball.COOLDOWN);

        this.reloadModule = new ReloadModule(this, InfernoWeaponInfo.CAPACITY, InfernoWeaponInfo.RELOAD_DURATION);
        this.fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
        this.burning = new Burning(combatUser, InfernoWeaponInfo.FIRE_DAMAGE_PER_SECOND, true);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.LEFT_CLICK, ActionKey.DROP};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        return reloadModule.getActionBarProgressBar(10, '■');
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return actionKey == ActionKey.DROP ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey);
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

                if (combatUser.getActionManager().getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(1);

                InfernoWeaponInfo.Sounds.USE.play(combatUser.getLocation());

                break;
            }
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() < InfernoWeaponInfo.Fireball.CAPACITY_CONSUME) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                new InfernoWeaponLProjectile().shot();

                if (combatUser.getActionManager().getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(InfernoWeaponInfo.Fireball.CAPACITY_CONSUME);

                CombatUtil.sendRecoil(combatUser, InfernoWeaponInfo.Fireball.Recoil.UP, InfernoWeaponInfo.Fireball.Recoil.SIDE,
                        InfernoWeaponInfo.Fireball.Recoil.UP_SPREAD, InfernoWeaponInfo.Fireball.Recoil.SIDE_SPREAD, 3, 1);
                InfernoWeaponInfo.Sounds.USE_FIREBALL.play(combatUser.getLocation());

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
        InfernoWeaponInfo.Sounds.RELOAD.play(i, combatUser.getLocation());
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
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(9, location -> {
                double distance = getTravelDistance();
                if (distance > 5)
                    return;

                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                InfernoWeaponInfo.Particles.BULLET_TRAIL.play(loc, getVelocity(), distance / 5.0);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                InfernoWeaponInfo.Particles.HIT_BLOCK.play(location);
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

                InfernoWeaponInfo.Particles.HIT_ENTITY.play(location);
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
        protected void onDestroy(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new InfernoWeaponLArea().emit(loc);

            InfernoWeaponInfo.Sounds.FIREBALL_EXPLODE.play(loc);
            InfernoWeaponInfo.Particles.FIREBALL_EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                InfernoWeaponInfo.Particles.BULLET_TRAIL_FIREBALL.play(loc);
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
                    ((Movable) target).getMoveModule().knockback(dir);
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
                double damage = CombatUtil.getDistantDamage(InfernoWeaponInfo.Fireball.DAMAGE_EXPLODE, distance, radius / 2.0);
                Timespan burningDuration = Timespan.ofTicks((long) CombatUtil.getDistantDamage(InfernoWeaponInfo.FIRE_DURATION.toTicks(), distance,
                        radius / 2.0));

                if (target.getDamageModule().damage(InfernoWeaponLProjectile.this, damage, DamageType.NORMAL, null, false, true)) {
                    target.getStatusEffectModule().apply(burning, burningDuration);

                    if (target instanceof Movable && isNotHit(target)) {
                        Vector dir = LocationUtil.getDirection(center, location.add(0, 0.5, 0)).multiply(InfernoWeaponInfo.Fireball.KNOCKBACK);
                        ((Movable) target).getMoveModule().knockback(dir);
                    }
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
