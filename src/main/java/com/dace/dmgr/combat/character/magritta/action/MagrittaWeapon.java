package com.dace.dmgr.combat.character.magritta.action;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Property;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashMap;

public final class MagrittaWeapon extends AbstractWeapon implements Reloadable {
    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 블록 명중 횟수 */
    private int blockHitCount = 0;

    public MagrittaWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, MagrittaWeaponInfo.getInstance());

        reloadModule = new ReloadModule(this, MagrittaWeaponInfo.CAPACITY, MagrittaWeaponInfo.RELOAD_DURATION);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.DROP};
    }

    @Override
    public long getDefaultCooldown() {
        return MagrittaWeaponInfo.COOLDOWN;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return (actionKey == ActionKey.DROP ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey))
                && combatUser.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && combatUser.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case LEFT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                setCooldown();

                Location loc = combatUser.getLocation();
                HashMap<Damageable, Integer> targets = new HashMap<>();

                new MagrittaWeaponHitscan(targets).shot();
                for (int i = 0; i < MagrittaWeaponInfo.PELLET_AMOUNT - 1; i++) {
                    Vector dir = VectorUtil.getSpreadedVector(loc.getDirection(), MagrittaWeaponInfo.SPREAD);
                    new MagrittaWeaponHitscan(targets).shot(dir);
                }
                targets.forEach((target, hits) -> {
                    if (hits >= MagrittaWeaponInfo.PELLET_AMOUNT / 2)
                        MagrittaT1.addShreddingValue(combatUser, target);
                });
                blockHitCount = 0;
                reloadModule.consume(1);

                CombatUtil.sendRecoil(combatUser, MagrittaWeaponInfo.RECOIL.UP, MagrittaWeaponInfo.RECOIL.SIDE, MagrittaWeaponInfo.RECOIL.UP_SPREAD,
                        MagrittaWeaponInfo.RECOIL.SIDE_SPREAD, 3, 1);
                MagrittaWeaponInfo.SOUND.USE.play(loc);
                TaskUtil.addTask(this, new DelayTask(() -> CombatEffectUtil.SHOTGUN_SHELL_DROP_SOUND.play(loc), 8));

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
        return reloadModule.getRemainingAmmo() < MagrittaWeaponInfo.CAPACITY;
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
        MagrittaWeaponInfo.SOUND.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    public void reset() {
        super.reset();

        reloadModule.setRemainingAmmo(MagrittaWeaponInfo.CAPACITY);
    }

    private final class MagrittaWeaponHitscan extends Hitscan<Damageable> {
        private final HashMap<Damageable, Integer> targets;

        private MagrittaWeaponHitscan(@NonNull HashMap<Damageable, Integer> targets) {
            super(combatUser, CombatUtil.EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(MagrittaWeaponInfo.DISTANCE).build());
            this.targets = targets;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            MagrittaWeaponInfo.PARTICLE.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(14, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                CombatEffectUtil.BULLET_TRAIL_PARTICLE.play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playSmallHitBlockParticle(location, hitBlock, 1);

                if (blockHitCount++ == 0) {
                    CombatEffectUtil.BULLET_HIT_BLOCK_SOUND.play(location);
                    CombatEffectUtil.playHitBlockSound(location, hitBlock, 1);
                }

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                double damage = CombatUtil.getDistantDamage(MagrittaWeaponInfo.DAMAGE, getTravelDistance(), MagrittaWeaponInfo.DISTANCE / 2.0);
                int shredding = target.getPropertyManager().getValue(Property.SHREDDING);
                if (shredding > 0)
                    damage = damage * (100 + MagrittaT1Info.DAMAGE_INCREMENT * shredding) / 100.0;
                if (target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, true))
                    targets.put(target, targets.getOrDefault(target, 0) + 1);

                MagrittaWeaponInfo.PARTICLE.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
