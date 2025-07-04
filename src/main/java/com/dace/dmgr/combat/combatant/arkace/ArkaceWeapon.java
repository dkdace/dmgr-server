package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.handler.DropHandler;
import com.dace.dmgr.combat.ability.weapon.AbstractWeapon;
import com.dace.dmgr.combat.ability.weapon.FullAuto;
import com.dace.dmgr.combat.ability.weapon.Reloadable;
import com.dace.dmgr.combat.ability.weapon.module.FullAutoModule;
import com.dace.dmgr.combat.ability.weapon.module.GradualSpreadModule;
import com.dace.dmgr.combat.ability.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class ArkaceWeapon extends AbstractWeapon implements Reloadable, FullAuto, DropHandler {
    /** 재장전 모듈 */
    @NonNull
    @Getter
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    @Getter
    private final FullAutoModule fullAutoModule;
    /** 점진적 탄퍼짐 모듈 */
    private final GradualSpreadModule gradualSpreadModule;

    public ArkaceWeapon(@NonNull CombatUser combatUser, @NonNull ArkaceWeaponInfo weaponInfo) {
        super(combatUser, weaponInfo, Timespan.ZERO);

        this.reloadModule = new ReloadModule(this);
        this.fullAutoModule = new FullAutoModule(this);
        this.gradualSpreadModule = new GradualSpreadModule(ArkaceWeaponInfo.Spread.INCREMENT, ArkaceWeaponInfo.Spread.START,
                ArkaceWeaponInfo.Spread.MAX);
    }

    @Override
    @NonNull
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(getCapacity(), ActionBarDisplay.AMMO_BAR_SYMBOL).build();
    }

    @Override
    public void onRightClick() {
        if (cancelP1()) {
            setCooldown(ArkaceWeaponInfo.SPRINT_READY_DURATION);
            return;
        }

        boolean isUlt = !combatUser.getAbilityManager().getAbility(ArkaceUltInfo.getInstance()).isDurationFinished();
        if (!isUlt && !reloadModule.consume(1))
            return;

        Location loc = combatUser.getLocation();
        if (isUlt) {
            new ArkaceWeaponHitscan(true).shot();
            ArkaceUltInfo.Effects.SHOOT.play(loc);
        } else {
            new ArkaceWeaponHitscan(false)
                    .shot(VectorUtil.getSpreadedVector(loc.getDirection(), gradualSpreadModule.increaseSpread()));

            ArkaceWeaponInfo.RECOIL.send(combatUser);
            ArkaceWeaponInfo.Effects.USE.play(loc);

            addTask(new DelayTask(() -> ArkaceWeaponInfo.Effects.SHELL_DROP.play(loc), 8));
        }
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

    /**
     * 패시브 1번 스킬을 취소시킨다.
     *
     * @return 무기 사용 취소 여부
     */
    private boolean cancelP1() {
        ArkaceP1 skillp1 = combatUser.getAbilityManager().getAbility(ArkaceP1Info.getInstance());
        Timespan skillp1Cooldown = ArkaceWeaponInfo.SPRINT_READY_DURATION.plus(Timespan.ofTicks(2));

        if (skillp1.cancel()) {
            skillp1.setCooldown(skillp1Cooldown);
            return true;
        }

        skillp1.setCooldown(skillp1Cooldown);
        return false;
    }

    @Override
    public int getCapacity() {
        return ArkaceWeaponInfo.CAPACITY;
    }

    @Override
    @NonNull
    public Timespan getReloadDuration() {
        return ArkaceWeaponInfo.RELOAD_DURATION;
    }

    @Override
    public void onAmmoEmpty() {
        reloadModule.reload();
    }

    @Override
    public void onReloadTick(long i) {
        ArkaceWeaponInfo.Effects.RELOAD.apply(i).play(combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    @Override
    @NonNull
    public FireRate getFireRate() {
        return ArkaceWeaponInfo.FIRE_RATE;
    }

    private final class ArkaceWeaponHitscan extends Hitscan<Damageable> {
        private final boolean isUlt;

        private ArkaceWeaponHitscan(boolean isUlt) {
            super(combatUser, EntityCondition.enemy(combatUser));
            this.isUlt = isUlt;
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(14, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0.2, -0.2, 0);
                (isUlt ? ArkaceUltInfo.Effects.BULLET_TRAIL : CombatEffectUtil.BULLET_TRAIL_PARTICLE).play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.BULLET_HIT_EFFECT.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                double damage = ArkaceWeaponInfo.DAMAGE;
                if (isUlt)
                    combatUser.getAbilityManager().getAbility(ArkaceUltInfo.getInstance()).getBonusScoreModule()
                            .addTarget(target, ArkaceUltInfo.KILL_SCORE_TIME_LIMIT);
                else
                    damage = ArkaceWeaponInfo.DISTANT_DAMAGE.getDamage(getTravelDistance());

                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, isCrit, !isUlt);
                return false;
            });
        }
    }
}
