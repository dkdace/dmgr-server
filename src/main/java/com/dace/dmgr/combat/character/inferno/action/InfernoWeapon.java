package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporary.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

@Getter
public final class InfernoWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final FullAutoModule fullAutoModule;

    public InfernoWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, InfernoWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, InfernoWeaponInfo.CAPACITY, InfernoWeaponInfo.RELOAD_DURATION);
        fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, FireRate.RPM_1200);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.LEFT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return InfernoWeaponInfo.FIREBALL.COOLDOWN;
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

                Vector dir = VectorUtil.getSpreadedVector(combatUser.getLocation().getDirection(), InfernoWeaponInfo.SPREAD);
                new InfernoWeaponRProjectile().shot(dir);
                if (combatUser.getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(1);

                InfernoWeaponInfo.SOUND.USE.play(combatUser.getLocation());

                break;
            }
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() < InfernoWeaponInfo.FIREBALL.CAPACITY_CONSUME) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                new InfernoWeaponLProjectile().shot();
                if (combatUser.getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(InfernoWeaponInfo.FIREBALL.CAPACITY_CONSUME);

                CombatUtil.sendRecoil(combatUser, InfernoWeaponInfo.FIREBALL.RECOIL.UP, InfernoWeaponInfo.FIREBALL.RECOIL.SIDE,
                        InfernoWeaponInfo.FIREBALL.RECOIL.UP_SPREAD, InfernoWeaponInfo.FIREBALL.RECOIL.SIDE_SPREAD, 3, 1);
                InfernoWeaponInfo.SOUND.USE_FIREBALL.play(combatUser.getLocation());

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
    public void onCancelled() {
        super.onCancelled();
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

        onCancelled();
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        InfernoWeaponInfo.SOUND.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(InfernoWeaponInfo.CAPACITY);
    }

    /**
     * 화염 상태 효과 클래스.
     */
    private static final class InfernoWeaponBurning extends Burning {
        private static final InfernoWeaponBurning instance = new InfernoWeaponBurning();

        private InfernoWeaponBurning() {
            super(InfernoWeaponInfo.FIRE_DAMAGE_PER_SECOND, true);
        }
    }

    private final class InfernoWeaponRProjectile extends Projectile<Damageable> {
        private InfernoWeaponRProjectile() {
            super(combatUser, InfernoWeaponInfo.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
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
                InfernoWeaponInfo.PARTICLE.BULLET_TRAIL.play(loc, getVelocity(), distance / 5.0);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                InfernoWeaponInfo.PARTICLE.HIT_BLOCK.play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.DAMAGE_PER_SECOND / 20.0, DamageType.NORMAL, null,
                        false, true))
                    target.getStatusEffectModule().applyStatusEffect(combatUser, InfernoWeaponBurning.instance, InfernoWeaponInfo.FIRE_DURATION);

                InfernoWeaponInfo.PARTICLE.HIT_ENTITY.play(location);

                return true;
            };
        }
    }

    private final class InfernoWeaponLProjectile extends Projectile<Damageable> {
        private InfernoWeaponLProjectile() {
            super(combatUser, InfernoWeaponInfo.FIREBALL.VELOCITY, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().size(InfernoWeaponInfo.FIREBALL.SIZE).maxDistance(InfernoWeaponInfo.FIREBALL.DISTANCE).build());
        }

        @Override
        protected void onDestroy(@NonNull Location location) {
            Location loc = location.add(0, 0.1, 0);
            new InfernoWeaponLArea().emit(loc);

            InfernoWeaponInfo.SOUND.FIREBALL_EXPLODE.play(loc);
            InfernoWeaponInfo.PARTICLE.FIREBALL_EXPLODE.play(loc);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                InfernoWeaponInfo.PARTICLE.BULLET_TRAIL_FIREBALL.play(loc);
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
                target.getDamageModule().damage(combatUser, InfernoWeaponInfo.FIREBALL.DAMAGE_DIRECT, DamageType.NORMAL, location, false, true);
                return false;
            };
        }

        private final class InfernoWeaponLArea extends Area<Damageable> {
            private InfernoWeaponLArea() {
                super(combatUser, InfernoWeaponInfo.FIREBALL.RADIUS, InfernoWeaponLProjectile.this.entityCondition.include(combatUser));
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            protected boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);
                double damage = CombatUtil.getDistantDamage(InfernoWeaponInfo.FIREBALL.DAMAGE_EXPLODE, distance,
                        InfernoWeaponInfo.FIREBALL.RADIUS / 2.0);
                long burning = (long) CombatUtil.getDistantDamage(InfernoWeaponInfo.FIRE_DURATION, distance,
                        InfernoWeaponInfo.FIREBALL.RADIUS / 2.0);
                if (target.getDamageModule().damage(InfernoWeaponLProjectile.this, damage, DamageType.NORMAL, null,
                        false, true)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, InfernoWeaponBurning.instance, burning);
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0))
                            .multiply(InfernoWeaponInfo.FIREBALL.KNOCKBACK));
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
