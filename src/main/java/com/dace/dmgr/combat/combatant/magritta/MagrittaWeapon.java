package com.dace.dmgr.combat.combatant.magritta;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.handler.LeftClickHandler;
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

import java.util.HashMap;

@Getter
public final class MagrittaWeapon extends AbstractWeapon implements Reloadable, LeftClickHandler, DropHandler {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;

    public MagrittaWeapon(@NonNull CombatUser combatUser, @NonNull MagrittaWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, MagrittaWeaponInfo.COOLDOWN);
        this.reloadModule = new ReloadModule(this);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(getCapacity(), ActionBarDisplay.AMMO_BAR_BIG_SYMBOL).build();
    }

    @Override
    protected boolean canUse() {
        AbilityManager abilityManager = combatUser.getAbilityManager();
        return super.canUse() && abilityManager.getAbility(MagrittaA2Info.getInstance()).isDurationFinished()
                && abilityManager.getAbility(MagrittaUltInfo.getInstance()).isDurationFinished();
    }

    @Override
    public void onLeftClick() {
        if (!reloadModule.consume(1))
            return;

        setCooldown();
        shot(false);

        MagrittaWeaponInfo.RECOIL.send(combatUser);

        Location loc = combatUser.getLocation();
        MagrittaWeaponInfo.Effects.USE.play(loc);

        addTask(new DelayTask(() -> MagrittaWeaponInfo.Effects.BULLET_SHELL.play(loc), 8));
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
        return MagrittaWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return MagrittaWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onAmmoEmpty() {
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
                MagrittaT1.addValue(combatUser, target);
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
                MagrittaT1.ValueEffect valueEffect = target.getStatusEffectModule().get(MagrittaT1.ValueEffect.class);

                if (valueEffect != null)
                    damage = damage * (100 + MagrittaT1Info.DAMAGE_INCREMENT * valueEffect.getValue()) / 100.0;

                if (target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, true)) {
                    targets.put(target, targets.getOrDefault(target, 0) + 1);

                    if (isUlt && target.isGoalTarget())
                        combatUser.getAbilityManager().getAbility(MagrittaUltInfo.getInstance()).getBonusScoreModule()
                                .addTarget(target, MagrittaUltInfo.KILL_SCORE_TIME_LIMIT);
                }

                MagrittaWeaponInfo.Effects.HIT_ENTITY.play(location);

                return false;
            };
        }
    }
}
