package com.dace.dmgr.combat.combatant.jager;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.CombatEffectUtil;
import com.dace.dmgr.combat.action.ActionBarDisplay;
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

import java.util.EnumSet;
import java.util.Set;

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
    public ActionBarDisplay getActionBarDisplay() {
        return ActionBarDisplay.builder(this).ammoBar(reloadModule.getCapacity(), ActionBarDisplay.AMMO_BAR_BIG_SYMBOL).build();
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.LEFT_CLICK, ActionKey.RIGHT_CLICK, ActionKey.DROP);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getCooldownIgnoreActionKeys() {
        return EnumSet.of(ActionKey.RIGHT_CLICK, ActionKey.DROP);
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

                JagerWeaponInfo.Scope.RECOIL.send(combatUser);

                Location loc = combatUser.getLocation();
                JagerWeaponInfo.Effects.SCOPE_USE.play(loc);

                addTask(new DelayTask(() -> JagerWeaponInfo.Effects.SCOPE_BULLET_SHELL.play(loc), 8));

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
                CombatEffectUtil.BULLET_HIT_EFFECT.apply(hitBlock).play(location);
                return false;
            };
        }

        @Override
        @NonNull
        protected HitEntityHandler<Damageable> getHitEntityHandler() {
            return createCritHitEntityHandler((location, target, isCrit) -> {
                target.getDamageModule().damage(combatUser, JagerWeaponInfo.Scope.DISTANT_DAMAGE.getDamage(getTravelDistance()), DamageType.NORMAL,
                        location, isCrit, true);
                return false;
            });
        }
    }
}
