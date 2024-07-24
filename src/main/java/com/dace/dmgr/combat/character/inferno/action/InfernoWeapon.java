package com.dace.dmgr.combat.character.inferno.action;

import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.combat.entity.module.statuseffect.Burning;
import com.dace.dmgr.combat.entity.temporal.Barrier;
import com.dace.dmgr.combat.interaction.Area;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.combat.interaction.ProjectileOption;
import com.dace.dmgr.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@Getter
public final class InfernoWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "InfernoWeapon";
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final FullAutoModule fullAutoModule;

    InfernoWeapon(@NonNull CombatUser combatUser) {
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
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                Location loc = combatUser.getEntity().getLocation();
                Vector dir = VectorUtil.getSpreadedVector(combatUser.getEntity().getLocation().getDirection(), InfernoWeaponInfo.SPREAD);
                new InfernoWeaponRProjectile().shoot(dir);
                if (combatUser.getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(1);

                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_WEAPON_USE, loc);

                break;
            }
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                Location loc = combatUser.getEntity().getLocation();
                new InfernoWeaponLProjectile().shoot();
                if (combatUser.getSkill(InfernoUltInfo.getInstance()).isDurationFinished())
                    reloadModule.consume(InfernoWeaponInfo.FIREBALL.CAPACITY_CONSUME);

                SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_WEAPON_USE_FIREBALL, loc);
                CombatUtil.setRecoil(combatUser, InfernoWeaponInfo.FIREBALL.RECOIL.UP, InfernoWeaponInfo.FIREBALL.RECOIL.SIDE,
                        InfernoWeaponInfo.FIREBALL.RECOIL.UP_SPREAD, InfernoWeaponInfo.FIREBALL.RECOIL.SIDE_SPREAD, 3, 1);

                break;
            }
            case DROP: {
                onAmmoEmpty();

                break;
            }
        }
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        reloadModule.setReloading(false);
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
        switch ((int) i) {
            case 3:
                SoundUtil.play(Sound.ENTITY_VILLAGER_YES, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 6:
                SoundUtil.play(Sound.BLOCK_FIRE_EXTINGUISH, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 10:
                SoundUtil.play(Sound.BLOCK_PISTON_EXTEND, combatUser.getEntity().getLocation(), 0.6, 0.7);
                break;
            case 27:
                SoundUtil.play(Sound.ENTITY_VILLAGER_NO, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 30:
                SoundUtil.play(Sound.ENTITY_WOLF_SHAKE, combatUser.getEntity().getLocation(), 0.6, 0.5);
                break;
            case 44:
                SoundUtil.play(Sound.BLOCK_IRON_DOOR_OPEN, combatUser.getEntity().getLocation(), 0.6, 0.7);
                break;
            case 47:
                SoundUtil.play(Sound.ENTITY_IRONGOLEM_ATTACK, combatUser.getEntity().getLocation(), 0.6, 1.4);
                break;
        }
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class InfernoWeaponBurning extends Burning {
        private static final InfernoWeaponBurning instance = new InfernoWeaponBurning();

        @Override
        public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getDamageModule().getHealMultiplierStatus().addModifier(MODIFIER_ID, -InfernoT1Info.HEAL_DECREMENT);
        }

        @Override
        public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
            super.onTick(combatEntity, provider, i);

            if (i % 10 == 0 && combatEntity instanceof Damageable && provider instanceof Attacker &&
                    ((Damageable) combatEntity).getDamageModule().damage((Attacker) provider, InfernoWeaponInfo.FIRE_DAMAGE_PER_SECOND * 10 / 20,
                            DamageType.NORMAL, null, false, true))
                SoundUtil.playNamedSound(NamedSound.COMBAT_DAMAGE_BURNING, combatEntity.getEntity().getLocation());
        }

        @Override
        public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
            if (combatEntity instanceof Healable)
                ((Healable) combatEntity).getDamageModule().getHealMultiplierStatus().removeModifier(MODIFIER_ID);
        }
    }

    private final class InfernoWeaponRProjectile extends Projectile {
        private InfernoWeaponRProjectile() {
            super(combatUser, InfernoWeaponInfo.VELOCITY, ProjectileOption.builder().trailInterval(9)
                    .size(InfernoWeaponInfo.SIZE).maxDistance(InfernoWeaponInfo.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            double distance = location.distance(combatUser.getEntity().getEyeLocation());
            if (distance > 5)
                return;

            Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            ParticleUtil.play(Particle.FLAME, loc, 0, velocity.getX(), velocity.getY(), velocity.getZ(), 1.3 - distance * 0.1);
            ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 0, velocity.getX(), velocity.getY(), velocity.getZ(), 1.45);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            ParticleUtil.play(Particle.DRIP_LAVA, location, 2, 0.07, 0.07, 0.07, 0);
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.DAMAGE_PER_SECOND / 20, DamageType.NORMAL, null,
                    false, true)) {
                target.getStatusEffectModule().applyStatusEffect(combatUser, InfernoWeaponBurning.instance, InfernoWeaponInfo.FIRE_DURATION);

                combatUser.useAction(ActionKey.PERIODIC_1);
            }

            ParticleUtil.play(Particle.SMOKE_NORMAL, location, 3, 0.2, 0.2, 0.2, 0.05);

            return true;
        }
    }

    private final class InfernoWeaponLProjectile extends Projectile {
        private InfernoWeaponLProjectile() {
            super(combatUser, InfernoWeaponInfo.FIREBALL.VELOCITY, ProjectileOption.builder().trailInterval(13)
                    .size(InfernoWeaponInfo.FIREBALL.SIZE).maxDistance(InfernoWeaponInfo.FIREBALL.DISTANCE).condition(combatUser::isEnemy).build());
        }

        @Override
        protected void trail() {
            Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
            ParticleUtil.play(Particle.FLAME, loc, 10, 0.12, 0.12, 0.12, 0);
            ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 13, 0.15, 0.15, 0.15, 0.04);
        }

        @Override
        protected boolean onHitBlock(@NonNull Block hitBlock) {
            return false;
        }

        @Override
        protected boolean onHitEntity(@NonNull Damageable target, boolean isCrit) {
            if (target.getDamageModule().damage(combatUser, InfernoWeaponInfo.FIREBALL.DAMAGE_DIRECT, DamageType.NORMAL, location,
                    false, true))
                combatUser.useAction(ActionKey.PERIODIC_1);

            return false;
        }

        @Override
        protected void onDestroy() {
            Location loc = location.clone().add(0, 0.1, 0);
            Predicate<CombatEntity> condition = this.condition.or(combatEntity -> combatEntity == combatUser);
            CombatEntity[] targets = CombatUtil.getNearCombatEntities(combatUser.getGame(), loc, InfernoWeaponInfo.FIREBALL.RADIUS, condition);
            new InfernoWeaponLArea(condition, targets).emit(loc);

            SoundUtil.playNamedSound(NamedSound.COMBAT_INFERNO_WEAPON_FIREBALL_EXPLODE, loc);
            ParticleUtil.play(Particle.SMOKE_LARGE, loc, 40, 0.2, 0.2, 0.2, 0.1);
            ParticleUtil.play(Particle.SMOKE_NORMAL, loc, 80, 0.1, 0.1, 0.1, 0.15);
            ParticleUtil.play(Particle.LAVA, loc, 30, 0.3, 0.3, 0.3, 0);
            ParticleUtil.play(Particle.FLAME, loc, 80, 0.2, 0.2, 0.2, 0.1);
        }

        private final class InfernoWeaponLArea extends Area {
            private InfernoWeaponLArea(Predicate<CombatEntity> condition, CombatEntity[] targets) {
                super(combatUser, InfernoWeaponInfo.FIREBALL.RADIUS, condition, targets);
            }

            @Override
            protected boolean onHitBlock(@NonNull Location center, @NonNull Location location, @NonNull Block hitBlock) {
                return false;
            }

            @Override
            public boolean onHitEntity(@NonNull Location center, @NonNull Location location, @NonNull Damageable target) {
                double distance = center.distance(location);
                int damage = CombatUtil.getDistantDamage(InfernoWeaponInfo.FIREBALL.DAMAGE_EXPLODE, distance,
                        InfernoWeaponInfo.FIREBALL.RADIUS / 2.0, true);
                int burning = CombatUtil.getDistantDamage((int) InfernoWeaponInfo.FIRE_DURATION, distance,
                        InfernoWeaponInfo.FIREBALL.RADIUS / 2.0, true);
                if (target.getDamageModule().damage(InfernoWeaponLProjectile.this, damage, DamageType.NORMAL, null,
                        false, true)) {
                    target.getStatusEffectModule().applyStatusEffect(combatUser, InfernoWeaponBurning.instance, burning);
                    target.getKnockbackModule().knockback(LocationUtil.getDirection(center, location.add(0, 0.5, 0))
                            .multiply(InfernoWeaponInfo.FIREBALL.KNOCKBACK));

                    if (target != combatUser)
                        combatUser.useAction(ActionKey.PERIODIC_1);
                }

                return !(target instanceof Barrier);
            }
        }
    }
}
