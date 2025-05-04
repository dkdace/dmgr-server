package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.CombatUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.weapon.AbstractWeapon;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.module.ReloadModule;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.EntityCondition;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.interaction.Hitscan;
import com.dace.dmgr.util.location.LocationUtil;
import com.dace.dmgr.util.task.DelayTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

public final class JagerWeaponR extends AbstractWeapon implements Reloadable {
    /** 주무기 인스턴스 */
    private final JagerWeaponL mainWeapon;
    /** 재장전 모듈 */
    @Getter
    @NonNull
    private final ReloadModule reloadModule;

    JagerWeaponR(@NonNull CombatUser combatUser, @NonNull JagerWeaponL mainWeapon) {
        super(combatUser, JagerWeaponInfo.getInstance(), JagerWeaponInfo.COOLDOWN);

        this.mainWeapon = mainWeapon;
        this.reloadModule = new ReloadModule(this, JagerWeaponInfo.Scope.CAPACITY, Timespan.ZERO);
    }

    @Override
    @NonNull
    public String getActionBarString() {
        String text = reloadModule.getActionBarProgressBar(reloadModule.getCapacity(), '┃');
        if (mainWeapon.getSwapModule().isSwapped())
            text = "§a" + text;

        return text;
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return actionKey == ActionKey.DROP || actionKey == ActionKey.RIGHT_CLICK ? combatUser.isGlobalCooldownFinished() : super.canUse(actionKey);
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

                new JagerWeaponRHitscan().shot();

                reloadModule.consume(1);

                CombatUtil.sendRecoil(combatUser, JagerWeaponInfo.Scope.Recoil.UP, JagerWeaponInfo.Scope.Recoil.SIDE,
                        JagerWeaponInfo.Scope.Recoil.UP_SPREAD, JagerWeaponInfo.Scope.Recoil.SIDE_SPREAD, 2, 1);

                Location loc = combatUser.getLocation();
                JagerWeaponInfo.Sounds.USE_SCOPE.play(loc);

                addTask(new DelayTask(() -> CombatEffectUtil.SHELL_DROP_SOUND.play(loc, 1, 0.75), 8));

                break;
            }
            case RIGHT_CLICK: {
                cancel();
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
        mainWeapon.getAimModule().cancel();
        mainWeapon.getReloadModule().cancel();
        mainWeapon.getSwapModule().cancel();
        mainWeapon.getSwapModule().swap();
    }

    @Override
    public boolean canReload() {
        return false;
    }

    @Override
    public void onAmmoEmpty() {
        if (reloadModule.isReloading())
            return;

        cancel();
        addActionTask(new DelayTask(() -> mainWeapon.getReloadModule().reload(), getDefaultCooldown().toTicks()));
    }

    @Override
    public void onReloadTick(long i) {
        // 미사용
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    private final class JagerWeaponRHitscan extends Hitscan<Damageable> {
        private JagerWeaponRHitscan() {
            super(combatUser, EntityCondition.enemy(combatUser));
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(12, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, 0, -0.2, 0);
                CombatEffectUtil.BULLET_TRAIL_PARTICLE.play(loc);
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
                double damage = CombatUtil.getDistantDamage(JagerWeaponInfo.Scope.DAMAGE, getTravelDistance(), JagerWeaponInfo.Scope.DAMAGE_WEAKENING_DISTANCE);
                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, isCrit, true);

                return false;
            });
        }
    }
}
