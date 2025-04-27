package com.dace.dmgr.combat.combatant.arkace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.FullAuto;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.GradualSpreadModule;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

@Getter
public final class ArkaceWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final GradualSpreadModule fullAutoModule;

    public ArkaceWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceWeaponInfo.getInstance(), Timespan.ZERO);

        this.reloadModule = new ReloadModule(this, ArkaceWeaponInfo.CAPACITY, ArkaceWeaponInfo.RELOAD_DURATION);
        this.fullAutoModule = new GradualSpreadModule(this, ActionKey.RIGHT_CLICK, ArkaceWeaponInfo.FIRE_RATE, ArkaceWeaponInfo.Spread.INCREMENT,
                ArkaceWeaponInfo.Spread.START, ArkaceWeaponInfo.Spread.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        return reloadModule.getActionBarProgressBar(reloadModule.getCapacity(), '|');
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }
                if (cancelP1()) {
                    setCooldown(ArkaceWeaponInfo.SPRINT_READY_DURATION);
                    return;
                }

                Location loc = combatUser.getLocation();
                if (combatUser.getSkill(ArkaceUltInfo.getInstance()).isDurationFinished()) {
                    new ArkaceWeaponHitscan(false).shot(VectorUtil.getSpreadedVector(loc.getDirection(), fullAutoModule.increaseSpread()));

                    reloadModule.consume(1);

                    CombatUtil.sendRecoil(combatUser, ArkaceWeaponInfo.Recoil.UP, ArkaceWeaponInfo.Recoil.SIDE, ArkaceWeaponInfo.Recoil.UP_SPREAD,
                            ArkaceWeaponInfo.Recoil.SIDE_SPREAD, 2, 2);
                    ArkaceWeaponInfo.Sounds.USE.play(loc);

                    addTask(new DelayTask(() -> CombatEffectUtil.SHELL_DROP_SOUND.play(loc), 8));
                } else {
                    new ArkaceWeaponHitscan(true).shot();
                    ArkaceUltInfo.Sounds.SHOOT.play(loc);
                }

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

    /**
     * 패시브 1번 스킬을 취소시킨다.
     *
     * @return 무기 사용 취소 여부
     */
    private boolean cancelP1() {
        ArkaceP1 skillp1 = combatUser.getSkill(ArkaceP1Info.getInstance());
        Timespan skillp1Cooldown = ArkaceWeaponInfo.SPRINT_READY_DURATION.plus(Timespan.ofTicks(2));

        if (skillp1.cancel()) {
            skillp1.setCooldown(skillp1Cooldown);
            return true;
        }

        skillp1.setCooldown(skillp1Cooldown);
        return false;
    }

    @Override
    public boolean canReload() {
        return reloadModule.getRemainingAmmo() < reloadModule.getCapacity();
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
        ArkaceWeaponInfo.Sounds.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
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
                (isUlt ? ArkaceUltInfo.Particles.BULLET_TRAIL : CombatEffectUtil.BULLET_TRAIL_PARTICLE).play(loc);
            });
        }

        @Override
        @NonNull
        protected HitBlockHandler getHitBlockHandler() {
            return (location, hitBlock) -> {
                CombatEffectUtil.playBulletHitBlockEffect(location, hitBlock);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                double damage = ArkaceWeaponInfo.DAMAGE;
                if (isUlt)
                    combatUser.getSkill(ArkaceUltInfo.getInstance()).getBonusScoreModule().addTarget(target, ArkaceUltInfo.KILL_SCORE_TIME_LIMIT);
                else
                    damage = CombatUtil.getDistantDamage(damage, getTravelDistance(), ArkaceWeaponInfo.DAMAGE_WEAKENING_DISTANCE);

                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, isCrit, !isUlt);
                return false;
            });
        }
    }
}
