package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.AbilityManager;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Bullet;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;

public final class MagrittaWeapon extends AbstractWeapon implements Reloadable {
    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;

    public MagrittaWeapon(@NonNull CombatUser combatUser, @NonNull MagrittaWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, MagrittaWeaponInfo.COOLDOWN);
        this.reloadModule = new ReloadModule(this, MagrittaWeaponInfo.CAPACITY, MagrittaWeaponInfo.RELOAD_DURATION);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.LEFT_CLICK, ActionKey.DROP);
    }

    @Override
    @NonNull
    protected Set<@NonNull ActionKey> getCooldownIgnoreActionKeys() {
        return EnumSet.of(ActionKey.DROP);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(reloadModule.getCapacity(), ActionBarDisplay.AMMO_BAR_BIG_SYMBOL).build();
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse(actionKey) && abilityManager.getSkill(MagrittaA2Info.getInstance()).isDurationFinished()
                && abilityManager.getSkill(MagrittaUltInfo.getInstance()).isDurationFinished();
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
                shot(false);

                reloadModule.consume(1);

                MagrittaWeaponInfo.RECOIL.send(combatUser);

                Location loc = combatUser.getLocation();
                MagrittaWeaponInfo.Effects.USE.play(loc);

                addTask(new DelayTask(() -> MagrittaWeaponInfo.Effects.BULLET_SHELL.play(loc), 8));

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
        return reloadModule.getRemainingAmmo() < MagrittaWeaponInfo.CAPACITY;
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
        MagrittaWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    /**
     * 기본 무기 총알을 발사한다.
     *
     * @param isUlt 궁극기 여부
     */
    void shot(boolean isUlt) {
        HashMap<Damageable, Integer> targets = new HashMap<>();
        double spread = MagrittaWeaponInfo.SPREAD;
        if (isUlt)
            spread *= 1.25;

        Bullet.shotgun(i -> new MagrittaWeaponHitscan(targets, i == 0, isUlt), MagrittaWeaponInfo.PELLET_AMOUNT, spread);

        targets.forEach((target, hits) -> {
            if (hits >= MagrittaWeaponInfo.PELLET_AMOUNT / 2)
                MagrittaT1Util.addValue(combatUser, target);
        });
    }

    private final class MagrittaWeaponHitscan extends Hitscan<Damageable> {
        private final HashMap<Damageable, Integer> targets;
        private final boolean isFirst;
        private final boolean isUlt;

        private MagrittaWeaponHitscan(@NonNull HashMap<Damageable, Integer> targets, boolean isFirst, boolean isUlt) {
            super(combatUser, EntityCondition.enemy(combatUser), Option.builder().maxDistance(MagrittaWeaponInfo.DISTANCE).build());

            this.targets = targets;
            this.isFirst = isFirst;
            this.isUlt = isUlt;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            (isUlt ? MagrittaUltInfo.Effects.HIT : MagrittaWeaponInfo.Effects.HIT).play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler((isUlt ? 15 : 14), location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);

                (isUlt ? MagrittaUltInfo.Effects.BULLET_TRAIL : CombatEffectUtil.BULLET_TRAIL_PARTICLE).play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                MagrittaWeaponInfo.Effects.HIT_BLOCK_PARTICLE.apply(hitBlock).play(location);
                if (isFirst)
                    MagrittaWeaponInfo.Effects.HIT_BLOCK_SOUND.apply(hitBlock).play(location);

                if (isUlt)
                    MagrittaUltInfo.Effects.HIT_BLOCK.play(location);

                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return (location, target) -> {
                double damage = MagrittaWeaponInfo.DISTANT_DAMAGE.getDamage(getTravelDistance());
                MagrittaT1Util.ValueEffect valueEffect = target.getStatusEffectModule().get(MagrittaT1Util.ValueEffect.class);

                if (valueEffect != null)
                    damage = damage * (100 + MagrittaT1Info.DAMAGE_INCREMENT * valueEffect.getValue()) / 100.0;

                if (target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, true)) {
                    targets.put(target, targets.getOrDefault(target, 0) + 1);

                    if (isUlt && target.isGoalTarget())
                        combatUser.getAbilityManager().getSkill(MagrittaUltInfo.getInstance()).getBonusScoreModule()
                                .addTarget(target, MagrittaUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                MagrittaWeaponInfo.Effects.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
