package com.dace.dmgr.combat.combatant.metar;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.Timestamp;
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
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.combat.interaction.Projectile;
import com.dace.dmgr.util.LocationUtil;
import com.dace.dmgr.util.VectorUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;

@Getter
public final class MetarWeapon extends AbstractWeapon implements Reloadable, FullAuto {
    /** 수정자 */
    private static final AbilityStatus.Modifier MODIFIER = new AbilityStatus.Modifier(-MetarWeaponInfo.SLOW);

    /** 재장전 모듈 */
    @NonNull
    private final ReloadModule reloadModule;
    /** 연사 모듈 */
    @NonNull
    private final FullAutoModule fullAutoModule;
    /** 이동속도 감소 타임스탬프 */
    private Timestamp slowTimestamp = Timestamp.now();
    /** 발사 방향의 반대 방향 여부 */
    private boolean isOpposite = true;

    public MetarWeapon(@NonNull CombatUser combatUser) {
        super(combatUser, MetarWeaponInfo.getInstance(), Timespan.ZERO);

        this.reloadModule = new ReloadModule(this, MetarWeaponInfo.CAPACITY, MetarWeaponInfo.RELOAD_DURATION);
        this.fullAutoModule = new FullAutoModule(this, ActionKey.RIGHT_CLICK, MetarWeaponInfo.FIRE_RATE);

        addOnReset(() -> combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER));
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.RIGHT_CLICK, ActionKey.DROP};
    }

    @Override
    @NonNull
    public String getActionBarString() {
        return reloadModule.getActionBarProgressBar(10, '■');
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        switch (actionKey) {
            case RIGHT_CLICK: {
                if (reloadModule.getRemainingAmmo() == 0) {
                    onAmmoEmpty();
                    return;
                }

                isOpposite = !isOpposite;

                new MetarWeaponProjectile(isOpposite).shot(VectorUtil.getSpreadedVector(combatUser.getLocation().getDirection(), MetarWeaponInfo.SPREAD));

                reloadModule.consume(1);
                combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER);

                if (slowTimestamp.isBefore(Timestamp.now())) {
                    slowTimestamp = Timestamp.now().plus(MetarWeaponInfo.SLOW_DURATION);
                    addTask(new IntervalTask(i -> slowTimestamp.isAfter(Timestamp.now()), () ->
                            combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER), 1));
                } else
                    slowTimestamp = Timestamp.now().plus(MetarWeaponInfo.SLOW_DURATION);

                MetarWeaponInfo.Sounds.USE.play(combatUser.getLocation());
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
        MetarWeaponInfo.Sounds.RELOAD.play(i, combatUser.getLocation());
    }

    @Override
    public void onReloadFinished() {
        // 미사용
    }

    /**
     * 달리기 가능 여부를 확인한다.
     *
     * @return 달리기 가능 여부
     */
    boolean canSprint() {
        return slowTimestamp.isBefore(Timestamp.now());
    }

    private final class MetarWeaponProjectile extends Projectile<Damageable> {
        private final boolean isOpposite;

        private MetarWeaponProjectile(boolean isOpposite) {
            super(MetarWeapon.this, MetarWeaponInfo.VELOCITY, EntityCondition.enemy(combatUser),
                    Option.builder().maxDistance(MetarWeaponInfo.DISTANCE).build());
            this.isOpposite = isOpposite;
        }

        @Override
        protected void onHit(@NonNull Location location) {
            MetarWeaponInfo.Particles.HIT.play(location);
            MetarWeaponInfo.Sounds.HIT.play(location);
        }

        @Override
        @NonNull
        protected IntervalHandler getIntervalHandler() {
            return createPeriodIntervalHandler(13, location -> {
                Location loc = LocationUtil.getLocationFromOffset(location, isOpposite ? -0.25 : 0.25, -0.2, 0);
                MetarWeaponInfo.Particles.BULLET_TRAIL.play(loc);
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
                double damage = CombatUtil.getDistantDamage(MetarWeaponInfo.DAMAGE, getTravelDistance(), MetarWeaponInfo.DAMAGE_WEAKENING_DISTANCE);

                target.getDamageModule().damage(combatUser, damage, DamageType.NORMAL, location, false, true);
                return false;
            };
        }
    }
}
